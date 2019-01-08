package controllers

import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import definedStrings.testStrings.ControllerStrings._
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class UsersControllerTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]

  val tables = Seq(chatTable, userTable, emailTable, toAddressTable, ccTable, bccTable, loginTable)

  override def beforeAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.create): _*)), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.drop): _*)), Duration.Inf)
  }

  override def afterEach(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.delete): _*)), Duration.Inf)
  }

  override def beforeEach(): Unit = {
    //encrypted "12345" password
    Await.result(db.run(userTable += UserRow(EmailExample, EncryptedPasswordExample)), Duration.Inf)
    Await.result(db.run(loginTable +=
      LoginRow(EmailExample, TokenExample, System.currentTimeMillis() + 360000, true)), Duration.Inf)
  }

  /** POST /sign end-point */
  UsersController + SignInFunction should {
    InvalidJSONBodyBadRequest + CaseUsername in {
      val fakeRequest = FakeRequest(POST, SignInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "NOTusername": "pedro@hotmail.com",
            "password": "12345"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  UsersController + SignInFunction should {
    InvalidJSONBodyBadRequest + CasePassword in {
      val fakeRequest = FakeRequest(POST, SignInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "username": "pedro@hotmail.com",
            "NOTpassword": "12345"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  UsersController + SignInFunction should {
    InvalidJSONBodyBadRequest + CaseMissingPassword in {
      val fakeRequest = FakeRequest(POST, SignInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "username": "pedro@hotmail.com"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  UsersController + SignInFunction should {
    InvalidJSONBodyBadRequest + CaseMissingUsername in {
      val fakeRequest = FakeRequest(POST, SignInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "password": "12345"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  UsersController + SignInFunction should {
    InvalidEmailAddress in {
      val fakeRequest = FakeRequest(POST, SignInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "username": "pedro@hotmail",
            "password": "12345"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  UsersController + SignInFunction should {
    ValidJSONBodyCreated in {
      val fakeRequest = FakeRequest(POST, SignInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "username": "pedro@hotmail.com",
            "password": "12345"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe CREATED
    }
  }
  /** ----------------------------------------------- */

  /** POST /login end-point */

  UsersController + LoginFunction should {
    InvalidJSONBodyBadRequest + CaseUsername in {
      val fakeRequest = FakeRequest(POST, LogInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "NOTusername": "pedro@hotmail.com",
            "password": "12345"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  UsersController + LoginFunction should {
    InvalidJSONBodyBadRequest + CasePassword in {
      val fakeRequest = FakeRequest(POST, LogInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "username": "pedro@hotmail.com",
            "NOTpassword": "12345"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  UsersController + LoginFunction should {
    InvalidJSONBodyBadRequest + CaseMissingPassword in {
      val fakeRequest = FakeRequest(POST, LogInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "username": "pedro@hotmail.com"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  UsersController + LoginFunction should {
    InvalidJSONBodyBadRequest + CaseMissingUsername in {
      val fakeRequest = FakeRequest(POST, LogInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "password": "12345"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  UsersController + LoginFunction should {
    ValidTokenOk in {
      val fakeRequest = FakeRequest(POST, LogInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "username": "pedro@hotmail.com",
            "password": "12345"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  UsersController + LoginFunction should {
    MissMatchPasswordForbidden in {
      val fakeRequest = FakeRequest(POST, LogInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse("""
          {
            "username": "pedro@hotmail.com",
            "password": "???"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** PATCH /logout end-point */

  UsersController + LogoutFunction should {
    ValidTokenOk in {
      val fakeRequest = FakeRequest(PATCH, LogOutEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> TokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  UsersController + LogoutFunction should {
    AlreadyLoggedOutForbidden in {
      Await.result(db.run(
        loginTable += LoginRow(EmailExample, "b93907a7-b939-4b33-8899-6741e6054822", System.currentTimeMillis() + 360000, false)), Duration.Inf)
      val fakeRequest = FakeRequest(PATCH, LogOutEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> "b93907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  UsersController + LogoutFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(PATCH, LogOutEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> WrongTokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

}