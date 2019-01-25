package api.controllers

import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.mappings.UserMappings._
import database.mappings.DraftMappings._
import database.mappings.{ LoginRow, UserRow }
import database.properties.TestDBProperties
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json.parse
import play.api.test.FakeRequest
import play.api.test.Helpers.{ route, status, _ }
import slick.jdbc.H2Profile.api._
import definedStrings.testStrings.ControllerStrings._
import generators.Generator

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class ChatsControllerActionTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)

  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = TestDBProperties.db

  private val testGenerator = new Generator()
  private val chatIDExample = testGenerator.ID
  private val emailExample = testGenerator.emailAddress

  private val tables = Seq(chatTable, userTable, emailTable, destinationEmailTable, destinationDraftTable, loginTable, shareTable)

  override def beforeEach(): Unit = {

    Await.result(db.run(userTable += UserRow(emailExample, testGenerator.password)), Duration.Inf)
    //encrypted "12345" password
    Await.result(db.run(loginTable +=
      LoginRow(emailExample, testGenerator.token, System.currentTimeMillis() + 360000, active = true)), Duration.Inf)
  }

  override def beforeAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.create): _*)), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.drop): _*)), Duration.Inf)
  }

  override def afterEach(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.delete): _*)), Duration.Inf)
  }

  /** GET /chats end-point */

  "ChatsController" + "#inbox" should {
    "send a OK if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(GET, "/chats")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "ChatsController" + "#inbox" should {
    "send a OK if JSON header has a valid token and a valid body" in {
      val fakeRequest = FakeRequest(GET, "/chats?isTrash=true")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "ChatsController" + "#inbox" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(GET, "/chats")
        .withHeaders(HOST -> LocalHost, TokenKey -> new Generator().token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** GET /chats/:chatID/emails end-point */

  "ChatsController" + "#getEmails" should {
    "send a OK if JSON header has a valid token" in {

      val fakeRequest = FakeRequest(GET, "/chats/:chatID/emails")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "ChatsController" + "#getEmails" should {
    "send a OK if JSON header has a valid token and a valid body" in {

      val fakeRequest = FakeRequest(GET, "/chats/:chatID/emails?isTrash=true")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "ChatsController" + "#getEmails" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(GET, "/chats/:chatID/emails")
        .withHeaders(HOST -> LocalHost, TokenKey -> new Generator().token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** PATCH /chats/:chatID end-point */

  "ChatsController" + "#moveInOutTrash" should {
    "send a OK if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(PATCH, "/chats/:chatID")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          """
          {
            "toTrash" : "true"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  /** ----------------------------------------------- */

  /** POST /shares end-point */

  "ChatsController" + "#supervised" should {
    "send a BadRequest if JSON body has an invalid format:" + " case chatID" in {
      val fakeRequest = FakeRequest(POST, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          s"""
          {
            "NOTchatID" : "$chatIDExample",
            "supervisor" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController" + "#supervised" should {
    "send a BadRequest if JSON body has an invalid format:" + " case supervisor" in {
      val fakeRequest = FakeRequest(POST, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          s"""
          {
            "chatID" : "$chatIDExample",
            "NOTsupervisor" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController" + "#supervised" should {
    "send a BadRequest if JSON body has an invalid format:" + " case missing supervisor parameter" in {
      val fakeRequest = FakeRequest(POST, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          s"""
          {
            "chatID" : "$chatIDExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController" + "#supervised" should {
    "send a BadRequest if JSON body has an invalid format:" + " case missing parameter chatID" in {
      val fakeRequest = FakeRequest(POST, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          s"""
          {
            "supervisor" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController" + "#supervised" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(POST, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> new Generator().token)
        .withJsonBody(parse(
          s"""
          {
            "chatID" : "$chatIDExample",
            "supervisor" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  "ChatsController" + "#supervised" should {
    "send a OK if JSON header has a valid token" + " and a valid JSON body" in {
      val fakeRequest = FakeRequest(POST, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          s"""
          {
            "chatID" : "$chatIDExample",
            "supervisor" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }
  /** ----------------------------------------------- */

  /** GET /shares end-point */

  "ChatsController" + "#getShares" should {
    "send a OK if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(GET, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "ChatsController" + "#getShares" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(GET, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> new Generator().token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** GET /shares/:shareID/emails end-point */

  "ChatsController" + "#getSharedEmails" should {
    "send a OK if JSON header has a valid token" in {

      val fakeRequest = FakeRequest(GET, "/shares/:shareID/emails")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "ChatsController" + "#getSharedEmails" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(GET, "/shares/:shareID/emails")
        .withHeaders(HOST -> LocalHost, TokenKey -> new Generator().token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  /** ----------------------------------------------- */

  /** DELETE /shares end-point */

  "ChatsController" + "#takePermissions" should {
    "send a BadRequest if JSON body has an invalid format:" + " case chatID" in {
      val fakeRequest = FakeRequest(DELETE, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          s"""
          {
            "NOTchatID" : "$chatIDExample",
            "supervisor" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController" + "#takePermissions" should {
    "send a BadRequest if JSON body has an invalid format:" + " case supervisor" in {
      val fakeRequest = FakeRequest(DELETE, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          s"""
          {
            "chatID" : "$chatIDExample",
            "NOTsupervisor" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController" + "#takePermissions" should {
    "send a BadRequest if JSON body has an invalid format:" + " case missing supervisor parameter" in {
      val fakeRequest = FakeRequest(DELETE, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          s"""
          {
            "chatID" : "$chatIDExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController" + "#takePermissions" should {
    "send a BadRequest if JSON body has an invalid format:" + " case missing parameter chatID" in {
      val fakeRequest = FakeRequest(DELETE, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          s"""
          {
            "supervisor" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController" + "#takePermissions" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(DELETE, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> new Generator().token)
        .withJsonBody(parse(
          s"""
          {
            "chatID" : "$chatIDExample",
            "supervisor" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  "ChatsController" + "#takePermissions" should {
    "send a OK if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(DELETE, "/shares")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(
          s"""
          {
            "chatID" : "$chatIDExample",
            "supervisor" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }
  /** ----------------------------------------------- */

}
