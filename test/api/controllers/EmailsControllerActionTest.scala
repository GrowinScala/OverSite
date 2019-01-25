package api.controllers

import database.mappings.ChatMappings._
import database.mappings.DraftMappings.destinationDraftTable
import database.mappings.EmailMappings._
import database.mappings.UserMappings._
import database.mappings._
import database.properties.TestDBProperties
import database.repository.ChatRepositoryImpl
import definedStrings.AlgorithmStrings.MD5Algorithm
import definedStrings.testStrings.ControllerStrings._
import encryption.EncryptString
import generators.Generator
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

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class EmailsControllerActionTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = TestDBProperties.db

  private val testGenerator = new Generator()
  private val chatIDExample = testGenerator.ID
  private val emailIDExample = new Generator().ID
  private val emailExample = testGenerator.emailAddress
  private val wrongTokenExample = new Generator().token
  private val passwordExample = testGenerator.password
  private val dateExample = testGenerator.dateOf
  private val headerExample = testGenerator.header
  private val bodyExample = testGenerator.body

  private val toAddressesJsonExample = testGenerator.emailAddresses.mkString("\" , \"")
  private val bccJsonExample = new Generator().emailAddresses.mkString("\" , \"")
  private val ccJsonExample = new Generator().emailAddresses.mkString("\" , \"")

  private val encryptedPasswordExample = new EncryptString(passwordExample, MD5Algorithm).result.toString

  private val tables = Seq(chatTable, userTable, emailTable, destinationEmailTable, destinationDraftTable, loginTable, shareTable)

  override def beforeEach(): Unit = {

    Await.result(db.run(DBIO.seq(tables.map(_.delete): _*)), Duration.Inf)
    //encrypted "12345" password
    Await.result(db.run(userTable += UserRow(emailExample, encryptedPasswordExample)), Duration.Inf)
    Await.result(db.run(loginTable +=
      LoginRow(emailExample, testGenerator.token, System.currentTimeMillis() + 360000, active = true)), Duration.Inf)

  }

  override def beforeAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.create): _*)), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.drop): _*)), Duration.Inf)
  }

  /** POST /email end-point */

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseDateOf in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$WrongDateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseHeader in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$WrongHeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseBody in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$WrongBodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseSendNow in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$WrongSendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    ValidJSONBodyOk + CaseMissingChatID in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseMissingDateOf in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseMissingHeader in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseMissingBody in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    ValidJSONBodyOk + CaseMissingTo in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + EmailFunction should {
    ValidJSONBodyOk + CaseMissingBCC in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey": ["$toAddressesJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + EmailFunction should {
    ValidJSONBodyOk + CaseMissingCC in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" :"$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseMissingSendNow in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"]
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> wrongTokenExample)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  EmailsController + EmailFunction should {
    ValidTokenOk + AndJsonBody in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }
  /** ----------------------------------------------- */

  /** GET /emails/:status end-point */

  EmailsController + GetEmailsFunction should {
    ValidTokenOk + AndStatus + StatusDraft in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusDraft)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailsFunction should {
    ValidTokenOk + AndStatus + StatusReceived in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusReceived)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailsFunction should {
    ValidTokenOk + AndStatus + StatusSent in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusSent)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailsFunction should {
    UndefinedStatusOk in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailsFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> wrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /**  GET /emails/:status/:emailID  end-point */

  EmailsController + GetEmailFunction should {
    ValidTokenOk + AndStatus + StatusDraft in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + EmailIDUndefined + OptionalStatus + StatusDraft)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailFunction should {
    ValidTokenOk + AndStatus + StatusReceived in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + EmailIDUndefined + OptionalStatus + StatusReceived)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailFunction should {
    ValidTokenOk + AndStatus + StatusSent in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + EmailIDUndefined + OptionalStatus + StatusSent)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }
  // /emails/:emailID
  EmailsController + GetEmailFunction should {
    UndefinedStatusOk in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + EmailIDUndefined + OptionalStatus + StatusUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + GetEmailFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + EmailIDUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> wrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  EmailsController + GetEmailFunction should {
    ValidTokenOk + CaseEmptyStatus in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + EmailIDUndefined + OptionalStatus + "")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }
  /** ----------------------------------------------- */

  EmailsController + ToSentFunction should {
    ValidTokenOk + AndStatus + StatusDraft + AndHasToAddress in {
      Await.result(db.run(emailTable += EmailRow(emailIDExample, chatIDExample,
        emailExample, dateExample, headerExample, bodyExample, isTrash = false)), Duration.Inf)

      Await.result(db.run(
        destinationEmailTable += DestinationEmailRow(new Generator().ID, emailExample, Destination.ToAddress, isTrash = false)), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, EmailsEndpointRoute + emailIDExample + EndpointPatchSendStatus)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + ToSentFunction should {
    HasNoToAddressBadRequest + CaseSendStatus in {

      Await.result(db.run(emailTable += EmailRow(emailIDExample, chatIDExample,
        emailExample, dateExample, headerExample, bodyExample, isTrash = false)), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, s"$EmailsEndpointRoute$EmailIDUndefined$EndpointPatchSendStatus")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + MoveInOutTrashFunction should {
    HasNoToAddressBadRequest + CaseTrashStatus in {

      Await.result(db.run(emailTable += EmailRow(emailIDExample, chatIDExample,
        emailExample, dateExample, headerExample, bodyExample, isTrash = false)), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, s"$EmailsEndpointRoute$EmailIDUndefined$EndpointPatchTrashStatus")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + ToSentFunction should {
    UndefinedStatusOk in {

      Await.result(db.run(chatTable += ChatRow(chatIDExample, headerExample)), Duration.Inf)
      Await.result(db.run(emailTable += EmailRow(emailIDExample, chatIDExample,
        emailExample, dateExample, headerExample, bodyExample, isTrash = false)), Duration.Inf)
      Await.result(db.run(
        destinationEmailTable += DestinationEmailRow(new Generator().ID, emailExample, Destination.ToAddress, isTrash = false)), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, EmailsEndpointRoute + emailIDExample + EndpointPatchSendStatus)
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + MoveInOutTrashFunction should {
    UndefinedStatusOk + CaseTrashStatus in {

      Await.result(db.run(emailTable += EmailRow(emailIDExample, chatIDExample, emailExample, dateExample,
        headerExample, bodyExample, isTrash = false)), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, s"$EmailsEndpointRoute$emailIDExample$EndpointPatchTrashStatus")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }
  EmailsController + UpdateDraftFunction should {
    CaseUpdateStatus in {

      Await.result(db.run(emailTable += EmailRow(emailIDExample, chatIDExample, emailExample, dateExample,
        headerExample, bodyExample, isTrash = false)), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, s"$EmailsEndpointRoute$emailIDExample$EndpointPatchUpdateStatus")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "efe",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : false
          }
        """))

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + UpdateDraftFunction should {
    CaseUpdateStatus + " no existing email id" in {

      Await.result(db.run(emailTable += EmailRow(emailIDExample, chatIDExample, emailExample, dateExample,
        headerExample, bodyExample, isTrash = false)), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, s"$EmailsEndpointRoute$EmailIDUndefined$EndpointPatchUpdateStatus")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)
        .withJsonBody(parse(s"""
          {
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + ToSentFunction should {
    InvalidTokenForbidden + CaseSendStatus in {
      val fakeRequest = FakeRequest(PATCH, s"$EmailsEndpointRoute$EmailIDUndefined$EndpointPatchSendStatus")
        .withHeaders(HOST -> LocalHost, TokenKey -> wrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  EmailsController + MoveInOutTrashFunction should {
    InvalidTokenForbidden + CaseTrashStatus in {
      val fakeRequest = FakeRequest(PATCH, s"$EmailsEndpointRoute$EmailIDUndefined$EndpointPatchTrashStatus")
        .withHeaders(HOST -> LocalHost, TokenKey -> wrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  EmailsController + UpdateDraftFunction should {
    InvalidTokenForbidden + CaseUpdateStatus in {
      val fakeRequest = FakeRequest(PATCH, s"$EmailsEndpointRoute$EmailIDUndefined$EndpointPatchUpdateStatus")
        .withHeaders(HOST -> LocalHost, TokenKey -> wrongTokenExample)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$chatIDExample",
            "$DateOfKey" : "$dateExample",
            "$HeaderKey" : "$headerExample",
            "$BodyKey" : "$bodyExample",
            "$ToKey" : ["$toAddressesJsonExample"],
            "$BCCKey" : ["$bccJsonExample"],
            "$CCKey" : ["$ccJsonExample"],
            "$SendNowKey" : true
          }
        """))

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

}
