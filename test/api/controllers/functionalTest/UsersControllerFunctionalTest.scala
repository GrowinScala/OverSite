package api.controllers.functionalTest

import database.mappings.ChatMappings.{ chatTable, shareTable }
import database.mappings.DraftMappings.destinationDraftTable
import database.mappings.EmailMappings._
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import database.properties.TestDBProperties
import definedStrings.AlgorithmStrings._
import definedStrings.testStrings.ControllerStrings._
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

class UsersControllerFunctionalTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit private val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy private val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy private val injector: Injector = appBuilder.injector()
  lazy implicit private val db: Database = TestDBProperties.db

  private val testGenerator = new Generator()
  private val chatIDExample = testGenerator.ID
  private val emailExample = testGenerator.emailAddress
  private val invalidEmailExample = testGenerator.words.head
  private val passwordExample = testGenerator.password
  private val encryptedPasswordExample = new EncryptString(passwordExample, MD5Algorithm).result.toString
  private val wrongPasswordExample = new Generator().password
  private val tokenExample = new Generator().token

  private val tables = Seq(chatTable, userTable, emailTable, destinationEmailTable, destinationDraftTable, loginTable, shareTable)

  override def beforeAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.create): _*)), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.drop): _*)), Duration.Inf)
  }

  override def beforeEach(): Unit = {

    Await.result(db.run(DBIO.seq(tables.map(_.delete): _*)), Duration.Inf)
    Await.result(db.run(userTable += UserRow(emailExample, encryptedPasswordExample)), Duration.Inf)
    Await.result(db.run(loginTable +=
      LoginRow(emailExample, testGenerator.token, System.currentTimeMillis() + 360000, active = true)), Duration.Inf)
  }

  /** POST /sign end-point */
  "UsersController #signIn" should {
    "send a BadRequest if JSON body has an invalid format:" + " case username" in {
      val fakeRequest = FakeRequest(POST, "/signin")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "NOTusername" : "$emailExample",
            "password" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "UsersController #signIn" should {
    "send a BadRequest if JSON body has an invalid format:" + " case password" in {
      val fakeRequest = FakeRequest(POST, "/signin")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "username" : "$emailExample",
            "NOTpassword" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "UsersController #signIn" should {
    "send a BadRequest if JSON body has an invalid format:" + " case missing password parameter" in {
      val fakeRequest = FakeRequest(POST, "/signin")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "username" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "UsersController #signIn" should {
    "send a BadRequest if JSON body has an invalid format:" + " case missing username parameter" in {
      val fakeRequest = FakeRequest(POST, "/signin")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "password" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "UsersController #signIn" should {
    "send a BadRequest if username is not a valid email address" in {
      val fakeRequest = FakeRequest(POST, "/signin")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "username" : "$invalidEmailExample",
            "password" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "UsersController #signIn" should {
    "send a Created if JSON body has a valid format " in {
      val fakeRequest = FakeRequest(POST, "/signin")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "username" : "$emailExample",
            "password" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe CREATED
    }
  }
  /** ----------------------------------------------- */

  /** POST /login end-point */

  //ERROR 1 TIME
  "UsersController #logIn" should {
    "send a BadRequest if JSON body has an invalid format:" + " case username" in {
      val fakeRequest = FakeRequest(POST, "/login")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "NOTusername" : "$emailExample",
            "password" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "UsersController #logIn" should {
    "send a BadRequest if JSON body has an invalid format:" + " case password" in {
      val fakeRequest = FakeRequest(POST, "/login")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "username" : "$emailExample",
            "NOTpassword" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "UsersController #logIn" should {
    "send a BadRequest if JSON body has an invalid format:" + " case missing password parameter" in {
      val fakeRequest = FakeRequest(POST, "/login")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "username" : "$emailExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "UsersController #logIn" should {
    "send a BadRequest if JSON body has an invalid format:" + " case missing username parameter" in {
      val fakeRequest = FakeRequest(POST, "/login")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "password" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "UsersController #logIn" should {
    "send an Ok if username and password match" in {
      val fakeRequest = FakeRequest(POST, "/login")
        .withHeaders(HOST -> LocalHost, TokenKey -> tokenExample)
        .withJsonBody(parse(
          s"""
          {
            "username" : "$emailExample",
            "password" : "$passwordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "UsersController #logIn" should {
    "send a Forbidden if username and password doesn't match" in {
      val fakeRequest = FakeRequest(POST, "/login")
        .withHeaders(HOST -> LocalHost)
        .withJsonBody(parse(
          s"""
          {
            "username" : "$emailExample",
            "password" : "$wrongPasswordExample"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** PATCH /logout end-point */

  "UsersController #logOut" should {
    "send a OK if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(PATCH, "/logout")
        .withHeaders(HOST -> LocalHost, TokenKey -> testGenerator.token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "UsersController #logOut" should {
    "send a Forbidden if JSON header has a valid token but the user is already log out" in {
      Await.result(db.run(
        loginTable += LoginRow(emailExample, tokenExample, System.currentTimeMillis() + 360000, active = false)), Duration.Inf)
      val fakeRequest = FakeRequest(PATCH, "/logout")
        .withHeaders(HOST -> LocalHost, TokenKey -> tokenExample)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  "UsersController #logOut" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(PATCH, "/logout")
        .withHeaders(HOST -> LocalHost, TokenKey -> new Generator().token)

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

}