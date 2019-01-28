package api.controllers

import akka.stream.Materializer
import api.validators.TokenValidator
import database.repository._
import database.repository.fake.FakeUserRepositoryImpl
import definedStrings.ApiStrings._
import definedStrings.testStrings.ControllerStrings.{ LocalHost, TokenKey }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext

class UsersControllerUnitTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach with Results {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy implicit val mat: Materializer = UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[Materializer]

  val usersActions: UserRepository = new FakeUserRepositoryImpl()

  "UsersController #signIn" should {
    "send a Ok if JSON header has a valid token" in {
      val controller = new UsersController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        usersActions)
      val result = controller.signIn.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "username" -> "pluis@growin.pt",
          "password" -> ""))))
      status(result) mustBe CREATED
    }
  }

  "UsersController #signIn" should {
    "send a BadRequest if JSON body has an invalid email address" in {
      val controller = new UsersController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        usersActions)
      val result = controller.signIn.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "username" -> "pluis.growin.pt",
          "password" -> ""))))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe InvalidEmailAddressStatus

    }
  }

  "UsersController #signIn" should {
    "send a BadRequest if JSON header has a invalid body: case username" in {
      val controller = new UsersController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        usersActions)
      val result = controller.signIn.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "NOTusername" -> "pluis.growin.pt",
          "password" -> ""))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "UsersController #signIn" should {
    "send a BadRequest if JSON header has a invalid body: case password" in {
      val controller = new UsersController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        usersActions)
      val result = controller.signIn.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "username" -> "pluis.growin.pt",
          "NOTpassword" -> ""))))
      status(result) mustBe BAD_REQUEST
    }
  }

}