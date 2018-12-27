package controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json._
import play.api.test.FakeRequest
import play.api.test.Helpers._

class UsersControllerTest extends PlaySpec with GuiceOneAppPerSuite {

  /** Sign in end-point */
  "UsersController Page#signIn" should {
    "should send a BadRequest if JSON body has an invalid format" in {
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

  "UsersController #signIn" should {
    "should send a BadRequest if username is not a valid email address" in {
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
    "should send a Created if JSON body has a valid format " in {
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
    "should send a BadRequest if JSON body has an invalid format" in {
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
    "should send an Ok if JSON body has a valid format" in {
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
    "should send a Forbidden if username and password doesn´t match" in {
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
    "should send an Ok if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(PATCH, s"/logout")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "UsersController #logOut" should {
    "should send an Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(PATCH, s"/logout")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "???")

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  /** ----------------------------------------------- */
}