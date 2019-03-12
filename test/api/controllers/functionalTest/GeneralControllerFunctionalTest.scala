package api.controllers.functionalTest

import database.mappings.ChatMappings.{ chatTable, shareTable }
import database.mappings.DraftMappings._
import database.mappings.EmailMappings._
import database.mappings.UserMappings._
import database.properties.TestDBProperties
import definedStrings.testStrings.ControllerStrings._
import generators.Generator
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json._
import play.api.libs.json.{ JsObject, JsString, Json }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class GeneralControllerFunctionalTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit private val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy implicit private val db: Database = TestDBProperties.db

  private val tables = Seq(chatTable, draftTable, userTable, emailTable, destinationEmailTable, destinationDraftTable, loginTable, shareTable)

  override def beforeAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.create): _*)), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.drop): _*)), Duration.Inf)
  }

  override def beforeEach(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.delete): _*)), Duration.Inf)
  }

  /** POST /sign end-point */
  "Functional test" should {
    " Able to signin and login users " +
      "\n create a draft and turn it into an email " +
      "\n get the emails sent and received from different users " +
      "\n give permission to a user from a chat and that user able to see those emails " in {

        /* User 1*/
        val testGenerator1 = new Generator()
        val usernameUser1 = testGenerator1.emailAddress
        val passwordUser1 = testGenerator1.password

        /* User 2*/
        val testGenerator2 = new Generator()
        val usernameUser2 = testGenerator2.emailAddress
        val passwordUser2 = testGenerator2.password

        /* User 3*/
        val testGenerator3 = new Generator()
        val usernameUser3 = testGenerator3.emailAddress
        val passwordUser3 = testGenerator3.password

        val draftUser1: JsObject = Json.obj(
          ChatIDKey -> testGenerator1.ID,
          DateOfKey -> testGenerator1.dateOf,
          HeaderKey -> testGenerator1.header,
          BodyKey -> testGenerator1.body,
          ToKey -> (usernameUser2 +: testGenerator1.emailAddresses),
          BCCKey -> testGenerator1.emailAddresses,
          CCKey -> testGenerator1.emailAddresses)

        val user1Credentials: JsObject = Json.obj(
          UsernameKey -> usernameUser1,
          PasswordKey -> passwordUser1)

        val user2Credentials: JsObject = Json.obj(
          UsernameKey -> usernameUser2,
          PasswordKey -> passwordUser2)

        val user3Credentials: JsObject = Json.obj(
          UsernameKey -> usernameUser3,
          PasswordKey -> passwordUser3)

        /** SignIn of User 1*/
        val signInUser1 = FakeRequest(POST, "/signin")
          .withHeaders(HOST -> LocalHost)
          .withBody(Json.toJson(user1Credentials))
        status(route(app, signInUser1).get) mustBe CREATED

        /** SignIn of User 2*/
        val signInUser2 = FakeRequest(POST, "/signin")
          .withHeaders(HOST -> LocalHost)
          .withBody(Json.toJson(user2Credentials))
        status(route(app, signInUser2).get) mustBe CREATED

        /** SignIn of User 3*/
        val signInUser3 = FakeRequest(POST, "/signin")
          .withHeaders(HOST -> LocalHost)
          .withBody(Json.toJson(user3Credentials))
        status(route(app, signInUser3).get) mustBe CREATED

        /** LogIn of User 1*/
        val logInUser1 = FakeRequest(POST, "/login")
          .withHeaders(HOST -> LocalHost)
          .withBody(Json.toJson(user1Credentials))
        status(route(app, logInUser1).get) mustBe OK
        val tokenUser1 = contentAsJson(route(app, logInUser1).get).\(fieldName = "Token").as[JsString].value

        /** LogIn of User 2*/
        val logInUser2 = FakeRequest(POST, "/login")
          .withHeaders(HOST -> LocalHost)
          .withBody(Json.toJson(user2Credentials))
        status(route(app, logInUser2).get) mustBe OK
        val tokenUser2 = contentAsJson(route(app, logInUser2).get).\(fieldName = "Token").as[JsString].value

        /** LogIn of User 3*/
        val logInUser3 = FakeRequest(POST, "/login")
          .withHeaders(HOST -> LocalHost)
          .withBody(Json.toJson(user3Credentials))
        status(route(app, logInUser3).get) mustBe OK
        val tokenUser3 = contentAsJson(route(app, logInUser3).get).\(fieldName = "Token").as[JsString].value

        /** Draft is saved by user1 with user2 as a destination*/
        val insertDraftUser1 = FakeRequest(POST, "/draft")
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
          .withBody(Json.toJson(draftUser1))
        status(route(app, insertDraftUser1).get) mustBe OK

        /** Gets draft by user 1 to reach the draftID*/
        val getDraftsUser1 = FakeRequest(GET, "/drafts?isTrash=false")
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
        status(route(app, getDraftsUser1).get) mustBe OK
        val draftIDUser1 = contentAsJson(route(app, getDraftsUser1).get)
          .head.\(fieldName = "Id").as[JsString].value

        /** Gets the draft with that specific draftID*/
        val getTargetDraftUser1 = FakeRequest(GET, "/drafts/" + draftIDUser1)
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
        status(route(app, getTargetDraftUser1).get) mustBe OK
        val draft = contentAsJson(route(app, getTargetDraftUser1).get)
        draft.\(fieldName = "toAddresses") mustBe draftUser1.\(fieldName = "to")
        draft.\(fieldName = "ccs") mustBe draftUser1.\(fieldName = "CC")
        draft.\(fieldName = "bccs") mustBe draftUser1.\(fieldName = "BCC")
        draft.\(fieldName = "header") mustBe draftUser1.\(fieldName = "header")
        draft.\(fieldName = "body") mustBe draftUser1.\(fieldName = "body")
        draft.\(fieldName = "draftID").as[String] mustBe draftIDUser1

        /** Turns the draft to trash */
        val moveToTrashUser1 = FakeRequest(PATCH, "/draft/" + draftIDUser1)
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
          .withBody(Json.toJson(Json.obj(
            fields = "status" -> "trash")))
        status(route(app, moveToTrashUser1).get) mustBe OK

        /** Gets all drafts in trash*/
        val getTrashDraftsUser1 = FakeRequest(GET, "/drafts?isTrash=true")
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
        status(route(app, getTrashDraftsUser1).get) mustBe OK
        val draftIDTrashUser1 = contentAsJson(route(app, getTrashDraftsUser1).get).head.\(fieldName = "Id").as[JsString].value
        draftIDUser1 mustEqual draftIDTrashUser1

        /** Turns the trash to draft */
        val moveToDraftUser1 = FakeRequest(PATCH, "/draft/" + draftIDUser1)
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
          .withBody(Json.toJson(Json.obj(
            fields = "status" -> "draft")))
        status(route(app, moveToDraftUser1).get) mustBe OK

        /** Gets all drafts not in trash*/
        val getNotTrashDraftsUser1 = FakeRequest(GET, "/drafts?isTrash=false")
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
        status(route(app, getNotTrashDraftsUser1).get) mustBe OK
        val draftIDNotTrashUser1 = contentAsJson(route(app, getNotTrashDraftsUser1).get).head.\(fieldName = "Id").as[JsString].value
        draftIDUser1 mustEqual draftIDNotTrashUser1

        /** Send draft from user 1 to user 2*/

        val sendDraftToEmail = FakeRequest(PATCH, "/draft/" + draftIDUser1)
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
          .withBody(Json.toJson(Json.obj(fields = "status" -> "send")))

        status(route(app, sendDraftToEmail).get) mustBe OK

        /** Gets the emails sent by user 1*/
        val getEmailsSentUser1 = FakeRequest(GET, "/emails?status=sent")
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
        Await.result(route(app, getEmailsSentUser1).get, Duration.Inf)
        status(route(app, getEmailsSentUser1).get) mustBe OK
        val emailIDUser1 = contentAsJson(route(app, getEmailsSentUser1).get).head.\(fieldName = "Id").as[JsString].value

        /** Gets emails received by user 2*/
        val getEmailsReceivedUser2 = FakeRequest(GET, "/emails?status=received")
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser2)
        status(route(app, getEmailsReceivedUser2).get) mustBe OK
        val emailIDUser2 = contentAsJson(route(app, getEmailsReceivedUser2).get).head.\(fieldName = "Id").as[JsString].value
        /** Verify if the emailIDs are the same*/
        emailIDUser1 mustEqual emailIDUser2

        /** Send email to trash by user 1*/
        val moveEmailToTrashUser1 = FakeRequest(PATCH, "/emails/" + emailIDUser1)
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
          .withBody(Json.toJson(Json.obj(
            fields = "toTrash" -> true)))
        status(route(app, moveEmailToTrashUser1).get) mustBe OK

        /** Gets emails sent by user1 (that are not in trash)*/
        val getEmailsSentUser1WithEmailInTrash = FakeRequest(GET, "/emails?status=sent")
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
        status(route(app, getEmailsSentUser1WithEmailInTrash).get) mustBe OK
        val emptyEmailIDUser1 = contentAsJson(route(app, getEmailsSentUser1WithEmailInTrash).get).as[Set[String]]
        /** Since the mail was moved to trash, the GET /emails should return an empty set*/
        emptyEmailIDUser1.isEmpty mustBe true

        /** Get target email with emailIDUser2 that has been received*/
        val getEmailUser2Received = FakeRequest(GET, "/emails/" + emailIDUser2)
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser2)
        status(route(app, getEmailUser2Received).get) mustBe OK
        val emailIDUser2Received = contentAsJson(route(app, getEmailUser2Received).get).\(fieldName = "emailID").as[JsString].value
        val chatIDUser2Received = contentAsJson(route(app, getEmailUser2Received).get).\(fieldName = "chatID").as[JsString].value
        val email = contentAsJson(route(app, getEmailUser2Received).get)

        /** Verify if the User 2 still have access to the email*/
        emailIDUser2Received mustEqual emailIDUser2

        /** User 1 give supervision to User 3 */
        val user1GivesPermissionToUser3 = FakeRequest(POST, "/shares")
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser1)
          .withBody(Json.toJson(Json.obj(
            fields = "chatID" -> chatIDUser2Received,
            "supervisor" -> usernameUser3)))
        status(route(app, user1GivesPermissionToUser3).get) mustBe OK

        /** User 3 will get supervised emails of User 1 */
        val getSupervisedEmails = FakeRequest(GET, "/shares")
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser3)
        status(route(app, getSupervisedEmails).get) mustBe OK
        val shareIDUser3 = contentAsJson(route(app, getSupervisedEmails).get).head.\(fieldName = "shareID").as[JsString].value

        /** User 3 will supervise a shareID of User 1 */
        val getSupervisedEmailsUser3 = FakeRequest(GET, "/shares/" + shareIDUser3)
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser3)
        status(route(app, getSupervisedEmailsUser3).get) mustBe OK
        val emailIDUser3 = contentAsJson(route(app, getSupervisedEmailsUser3).get).head.\(fieldName = "Id").as[JsString].value
        emailIDUser3 mustEqual emailIDUser2

        /** User 3 will get a email of User 1 */
        val getSharedEmailIDUser3 = FakeRequest(GET, "/shares/" + shareIDUser3 + "/email/" + emailIDUser3)
          .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> tokenUser3)
        status(route(app, getSharedEmailIDUser3).get) mustBe OK
        val sharedEmail = contentAsJson(route(app, getSharedEmailIDUser3).get)
        sharedEmail.\("emailID") mustBe email.\("emailID")
        sharedEmail.\("chatID") mustBe email.\("chatID")
        sharedEmail.\("fromAddress") mustBe email.\("fromAddress")
        sharedEmail.\("header") mustBe email.\("header")
        sharedEmail.\("body") mustBe email.\("body")
        sharedEmail.\("dateOf") mustBe email.\("dateOf")
        sharedEmail.\("username") mustBe email.\("username")

      }
  }

}
