package controllers

import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import actions.UserActions
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext
import scala.util.Try

class UsersControllerTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]

  val userActionsTest = new UserActions()

  override def beforeAll() = {
    userActionsTest.createFilesTable
  }

  override def afterAll() = {
    userActionsTest.dropFilesTable
  }

  override def beforeEach() = {
    //encrypted "12345" password
    db.run(userTable += UserRow("pedro@hotmail.com", "13012420314234138112108765216110414524878123"))
    db.run(loginTable += LoginRow("pedro@hotmail.com", "9e2907a7-b939-4b33-8899-6741e6054822", System.currentTimeMillis() + 360000, true))

  }

  override def afterEach() = {

    db.run(userTable.delete)
    db.run(loginTable.delete)

  }

  /** Sign in end-point */
  "UsersController Page#signIn" should {
    "send a BadRequest if JSON body has an invalid format" in {
      val fakeRequest = FakeRequest(POST, s"/signin")
        .withHeaders(HOST -> "localhost:9000")
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
  "UsersController Page#signIn" should {
    "send a BadRequest if JSON body has an invalid format 2" in {
      val fakeRequest = FakeRequest(POST, s"/signin")
        .withHeaders(HOST -> "localhost:9000")
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

  "UsersController #signIn" should {
    "send a BadRequest if username is not a valid email address" in {
      val fakeRequest = FakeRequest(POST, s"/signin")
        .withHeaders(HOST -> "localhost:9000")
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

  "UsersController #signIn" should {
    "send a Created if JSON body has a valid format " in {
      val fakeRequest = FakeRequest(POST, s"/signin")
        .withHeaders(HOST -> "localhost:9000")
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

  /** Login in end-point */
  "UsersController #logIn" should {
    "send a BadRequest if JSON body has an invalid format" in {
      val fakeRequest = FakeRequest(POST, s"/login")
        .withHeaders(HOST -> "localhost:9000")
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

  "UsersController #logIn" should {
    "send an Ok if JSON body has a valid format" in {
      val fakeRequest = FakeRequest(POST, s"/login")
        .withHeaders(HOST -> "localhost:9000")
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

  "UsersController #logIn" should {
    "send a Forbidden if username and password doesn t match" in {
      val fakeRequest = FakeRequest(POST, s"/login")
        .withHeaders(HOST -> "localhost:9000")
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

  /** Logout in end-point */

  "UsersController #logOut" should {
    "send an Ok if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(PATCH, s"/logout")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "UsersController #logOut" should {
    "send an Forbidden if JSON header has a valid token but the user is already log out" in {
      db.run(loginTable += LoginRow("pedro@hotmail.com", "b93907a7-b939-4b33-8899-6741e6054822", System.currentTimeMillis() + 360000, false))
      val fakeRequest = FakeRequest(PATCH, s"/logout")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "b93907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  "UsersController #logOut" should {
    "send an Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(PATCH, s"/logout")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "???")

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  /** ----------------------------------------------- */
}