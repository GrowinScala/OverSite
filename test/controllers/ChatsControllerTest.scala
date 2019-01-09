package controllers

import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import database.repository.{ ChatRepository, ChatRepositoryImpl }
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

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class ChatsControllerTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]
  lazy implicit val rep: ChatRepositoryImpl = new ChatRepositoryImpl()

  val tables = Seq(chatTable, userTable, emailTable, toAddressTable, ccTable, bccTable, loginTable, shareTable)

  override def beforeEach(): Unit = {
    //encrypted "12345" password
    Await.result(db.run(userTable += UserRow(EmailExample, EncryptedPasswordExample)), Duration.Inf)
    Await.result(db.run(loginTable +=
      LoginRow(EmailExample, TokenExample1, System.currentTimeMillis() + 360000, active = true)), Duration.Inf)
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

  ChatsController + InboxFunction should {
    ValidTokenOk in {
      val fakeRequest = FakeRequest(GET, ChatsEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  ChatsController + InboxFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(GET, ChatsEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** GET /chats/:chatID/emails end-point */

  ChatsController + GetEmailsFunction should {
    ValidTokenOk in {

      val fakeRequest = FakeRequest(GET, s"$ChatsEndpointRoute/$ChatIDUndefined$Emails")
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  ChatsController + GetEmailsFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(GET, s"$ChatsEndpointRoute/$ChatIDUndefined$Emails")
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** GET /chats/:chatID/emails/:emailID end-point */

  ChatsController + GetEmailFunction should {
    ValidTokenOk in {
      val fakeRequest = FakeRequest(GET, s"$ChatsEndpointRoute/$ChatIDUndefined$Emails/$EmailIDUndefined")
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  ChatsController + GetEmailFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(GET, s"$ChatsEndpointRoute/$ChatIDUndefined$Emails/$EmailIDUndefined")
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** POST /shares end-point */

  ChatsController + SupervisedFunction should {
    InvalidJSONBodyBadRequest + CaseChatID in {
      val fakeRequest = FakeRequest(POST, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)
        .withJsonBody(parse(s"""
          {
            "$WrongChatIDKey" : "$ChatIDExample",
            "$SupervisorKey" : "$EmailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  ChatsController + SupervisedFunction should {
    InvalidJSONBodyBadRequest + CaseSupervisor in {
      val fakeRequest = FakeRequest(POST, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$ChatIDExample",
            "$WrongSupervisorKey" : "$EmailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  ChatsController + SupervisedFunction should {
    InvalidJSONBodyBadRequest + CaseMissingSupervisor in {
      val fakeRequest = FakeRequest(POST, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$ChatIDExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  ChatsController + SupervisedFunction should {
    InvalidJSONBodyBadRequest + CaseMissingChatID in {
      val fakeRequest = FakeRequest(POST, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)
        .withJsonBody(parse(s"""
          {
            "$SupervisorKey" : "$EmailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  ChatsController + SupervisedFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(POST, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$ChatIDExample",
            "$SupervisorKey" : "$EmailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  ChatsController + SupervisedFunction should {
    ValidTokenOk + AndJsonBody in {
      val fakeRequest = FakeRequest(POST, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$ChatIDExample",
            "$SupervisorKey" : "$EmailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }
  /** ----------------------------------------------- */

  /** GET /shares end-point */

  ChatsController + SharesFunction should {
    ValidTokenOk in {
      val fakeRequest = FakeRequest(GET, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  ChatsController + SharesFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(GET, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** GET /shares/:shareID/emails end-point */

  ChatsController + GetSharedEmailsFunction should {
    ValidTokenOk in {

      val fakeRequest = FakeRequest(GET, s"$SharesEndpointRoute/$ShareIDUndefined$Emails")
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  ChatsController + GetSharedEmailsFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(GET, s"$SharesEndpointRoute/$ShareIDUndefined$Emails")
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** GET /shares/:shareID/emails/:emailID end-point */

  ChatsController + GetSharedEmailFunction should {
    ValidTokenOk in {

      val fakeRequest = FakeRequest(GET, s"$SharesEndpointRoute/$ShareIDUndefined$Emails/$EmailIDUndefined")
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  ChatsController + GetSharedEmailFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(GET, s"$SharesEndpointRoute/$ShareIDUndefined$Emails/$EmailIDUndefined")
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** DELETE /shares end-point */

  ChatsController + TakePermissionsFunction should {
    InvalidJSONBodyBadRequest + CaseChatID in {
      val fakeRequest = FakeRequest(DELETE, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)
        .withJsonBody(parse(s"""
          {
            "$WrongChatIDKey" : "$ChatIDExample",
            "$SupervisorKey" : "$EmailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  ChatsController + TakePermissionsFunction should {
    InvalidJSONBodyBadRequest + CaseSupervisor in {
      val fakeRequest = FakeRequest(DELETE, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$ChatIDExample",
            "$WrongSupervisorKey" : "$EmailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  ChatsController + TakePermissionsFunction should {
    InvalidJSONBodyBadRequest + CaseMissingSupervisor in {
      val fakeRequest = FakeRequest(DELETE, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$ChatIDExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  ChatsController + TakePermissionsFunction should {
    InvalidJSONBodyBadRequest + CaseMissingChatID in {
      val fakeRequest = FakeRequest(DELETE, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)
        .withJsonBody(parse(s"""
          {
            "$SupervisorKey" : "$EmailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  ChatsController + TakePermissionsFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(DELETE, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$ChatIDExample",
            "$SupervisorKey" : "$EmailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  ChatsController + TakePermissionsFunction should {
    ValidTokenOk in {
      val fakeRequest = FakeRequest(DELETE, SharesEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample1)
        .withJsonBody(parse(s"""
          {
            "$ChatIDKey" : "$ChatIDExample",
            "$SupervisorKey" : "$EmailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }
  /** ----------------------------------------------- */

}
