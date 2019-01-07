package controllers
import actions.ChatActions
import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.mappings.UserMappings._
import database.mappings._
import database.repository.{ ChatRepository, ChatRepositoryImpl }
import definedStrings.testStrings.ControllerStrings._
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

class EmailsControllerTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]
  lazy implicit val rep = new ChatRepositoryImpl()
  val chatActionsTest = new ChatActions()

  val tables = Seq(chatTable, userTable, emailTable, toAddressTable, ccTable, bccTable, loginTable, shareTable)

  override def beforeEach(): Unit = {
    //encrypted "12345" password
    Await.result(db.run(userTable += UserRow(EmailExample, EncryptedPasswordExample)), Duration.Inf)
    Await.result(db.run(loginTable +=
      LoginRow(EmailExample, TokenExample, System.currentTimeMillis() + 360000, true)), Duration.Inf)
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

  /** POST /email end-point */

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseDateOf in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "NOTdateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseHeader in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "NOTheader": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseBody in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "NOTbody": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseSendNow in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "NOTsendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    ValidJSONBodyOk + CaseMissingChatID in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseMissingDateOf in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseMissingHeader in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseMissingBody in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    ValidJSONBodyOk + CaseMissingTo in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + EmailFunction should {
    ValidJSONBodyOk + CaseMissingBCC in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + EmailFunction should {
    ValidJSONBodyOk + CaseMissingCC in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + EmailFunction should {
    InvalidJSONBodyBadRequest + CaseMissingSendNow in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"]
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + EmailFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  EmailsController + EmailFunction should {
    ValidTokenOk + AndJsonBody in {
      val fakeRequest = FakeRequest(POST, EmailEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
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
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailsFunction should {
    ValidTokenOk + AndStatus + StatusReceived in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusReceived)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailsFunction should {
    ValidTokenOk + AndStatus + StatusSent in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusSent)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailsFunction should {
    InvalidStatusBadRequest in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + GetEmailsFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /**  GET /emails/:status/:emailID  end-point */

  EmailsController + GetEmailFunction should {
    ValidTokenOk + AndStatus + StatusDraft in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusDraft + "/" + EmailIDUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailFunction should {
    ValidTokenOk + AndStatus + StatusReceived in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusReceived + "/" + EmailIDUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailFunction should {
    ValidTokenOk + AndStatus + StatusSent in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusSent + "/" + EmailIDUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + GetEmailFunction should {
    InvalidStatusBadRequest in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusUndefined + "/" + EmailIDUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + GetEmailFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(GET, EmailsEndpointRoute + StatusUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /**  PATCH /emails/:status/:emailID  end-point */

  EmailsController + ToSentFunction should {
    ValidTokenOk + AndStatus + StatusDraft + AndHasToAddress in {
      Await.result(db.run(emailTable += EmailRow(EmailIDExample, "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
        EmailExample, "2018-12-01", "Hello World!", "Have a good day Sir", false)), Duration.Inf)

      Await.result(db.run(toAddressTable += ToAddressRow(
        "4d192fff-f787-4d19-926c-1ba62fd03a9a",
        EmailIDExample, "vfernandesgrowin.pt")), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, EmailsEndpointRoute + StatusDraft + "/" + EmailIDExample)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  EmailsController + ToSentFunction should {
    HasNoToAddressBadRequest in {

      Await.result(db.run(emailTable += EmailRow(EmailIDExample, "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
        EmailExample, "2018-12-01", LocalHost, "Have a good day Sir", false)), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, EmailsEndpointRoute + StatusDraft + "/" + EmailIDExample)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + ToSentFunction should {
    InvalidStatusBadRequest in {

      val fakeRequest = FakeRequest(PATCH, EmailsEndpointRoute + StatusUndefined + "/" + EmailIDUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  EmailsController + ToSentFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(PATCH, EmailsEndpointRoute + StatusUndefined + "/" + EmailIDUndefined)
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

}
