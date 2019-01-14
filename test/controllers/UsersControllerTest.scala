package controllers

import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import definedStrings.testStrings.ControllerStrings._
import definedStrings.AlgorithmStrings._
import encryption.EncryptString
import generators.Generator
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

  private val testGenerator = new Generator()
  private val chatIDExample = testGenerator.ID
  private val emailExample = testGenerator.emailAddress
  private val invalidEmailExample = testGenerator.words.head
  private val passwordExample = testGenerator.password
  private val encryptedPasswordExample = new EncryptString(passwordExample, MD5Algorithm).result.toString
  private val wrongPasswordExample = new Generator().password
  private val tokenExample = new Generator().token

  private val tables = Seq(chatTable, userTable, emailTable, toAddressTable, ccTable, bccTable, loginTable)

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
    Await.result(db.run(userTable += UserRow(emailExample, encryptedPasswordExample)), Duration.Inf)
    Await.result(db.run(loginTable +=
      LoginRow(emailExample, testGenerator.token, System.currentTimeMillis() + 360000, active = true)), Duration.Inf)
  }

  /** POST /sign end-point */
  UsersController + SignInFunction should {
    InvalidJSONBodyBadRequest + CaseUsername in {
      val fakeRequest = FakeRequest(POST, SignInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(s"""
          {
            "$WrongUsernameKey" : "$emailExample",
            "$PasswordKey" : "$passwordExample"
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
        .withJsonBody(parse(s"""
          {
            "$UsernameKey" : "$emailExample",
            "$WrongPasswordKey" : "$passwordExample"
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
        .withJsonBody(parse(s"""
          {
            "$UsernameKey" : "$emailExample"
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
        .withJsonBody(parse(s"""
          {
            "$PasswordKey" : "$passwordExample"
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
        .withJsonBody(parse(s"""
          {
            "$UsernameKey" : "$invalidEmailExample",
            "$PasswordKey" : "$passwordExample"
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
        .withJsonBody(parse(s"""
          {
            "$UsernameKey" : "$emailExample",
            "$PasswordKey" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe CREATED
    }
  }
  /** ----------------------------------------------- */

  /** POST /login end-point */

  //ERROR 1 TIME
  UsersController + LoginFunction should {
    InvalidJSONBodyBadRequest + CaseUsername in {
      val fakeRequest = FakeRequest(POST, LogInEndpointRoute)
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(s"""
          {
            "$WrongUsernameKey" : "$emailExample",
            "$PasswordKey" : "$passwordExample"
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
        .withJsonBody(parse(s"""
          {
            "$UsernameKey" : "$emailExample",
            "$WrongPasswordKey" : "$passwordExample"
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
        .withJsonBody(parse(s"""
          {
            "$UsernameKey" : "$emailExample"
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
        .withJsonBody(parse(s"""
          {
            "$PasswordKey" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  UsersController + LoginFunction should {
    PasswordMatchOk in {
      val fakeRequest = FakeRequest(POST, LogInEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> tokenExample)
        .withJsonBody(parse(s"""
          {
            "$UsernameKey" : "$emailExample",
            "$PasswordKey" : "$passwordExample"
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
        .withJsonBody(parse(s"""
          {
            "$UsernameKey" : "$emailExample",
            "$PasswordKey" : "$wrongPasswordExample"
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
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  UsersController + LogoutFunction should {
    AlreadyLoggedOutForbidden in {
      Await.result(db.run(
        loginTable += LoginRow(emailExample, tokenExample, System.currentTimeMillis() + 360000, active = false)), Duration.Inf)
      val fakeRequest = FakeRequest(PATCH, LogOutEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> tokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  UsersController + LogoutFunction should {
    InvalidTokenForbidden in {
      val fakeRequest = FakeRequest(PATCH, LogOutEndpointRoute)
        .withHeaders(HOST -> LocalHost, TokenKey -> new Generator().token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

}