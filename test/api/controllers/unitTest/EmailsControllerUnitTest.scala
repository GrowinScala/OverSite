package api.controllers.unitTest

import database.repository.fake.{ FakeChatRepositoryImpl, FakeEmailRepositoryImpl, FakeUserRepositoryImpl }
import org.scalatest.{ Matchers, _ }
import akka.stream.Materializer
import api.controllers.EmailsController
import api.validators.TokenValidator
import database.repository._
import database.repository.fake.{ FakeEmailRepositoryImpl, FakeUserRepositoryImpl }
import definedStrings.ApiStrings._
import definedStrings.testStrings.ControllerStrings.{ TokenKey, _ }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import api.controllers.unitTest.UnitControllerTestsAppBuilder._

import scala.concurrent.ExecutionContext

class EmailsControllerUnitTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach with Results {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy implicit val mat: Materializer = UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[Materializer]

  val emailActions: EmailRepository = new FakeEmailRepositoryImpl()
  val userActions: UserRepository = new FakeUserRepositoryImpl()

  "EmailsController #email" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.email().apply(FakeRequest(POST, "/email")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2010-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "bcc" -> Seq(""),
          "cc" -> Seq("")))))
      status(result) mustBe OK
      contentAsString(result) mustBe MailSentStatus
    }
  }

  "EmailsController #email" should {
    "send a OK if JSON header has a valid token: case missing chatID" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.email().apply(FakeRequest(POST, "/email")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "dateOf" -> "2010-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "bcc" -> Seq(""),
          "cc" -> Seq("")))))
      status(result) mustBe OK
      contentAsString(result) mustBe MailSentStatus
    }
  }

  "EmailsController #email" should {
    "send a OK if JSON header has a valid token: case missing to" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.email().apply(FakeRequest(POST, "/email")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2010-10-10",
          "header" -> "",
          "body" -> "",
          "bcc" -> Seq(""),
          "cc" -> Seq("")))))
      status(result) mustBe OK
      contentAsString(result) mustBe MailSentStatus
    }
  }

  "EmailsController #email" should {
    "send a OK if JSON header has a valid token: case missing bcc" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.email().apply(FakeRequest(POST, "/email")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2010-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "cc" -> Seq("")))))
      status(result) mustBe OK
      contentAsString(result) mustBe MailSentStatus
    }
  }

  "EmailsController #email" should {
    "send a OK if JSON header has a valid token: case missing cc" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.email().apply(FakeRequest(POST, "/email")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2010-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "bcc" -> Seq("")))))
      status(result) mustBe OK
      contentAsString(result) mustBe MailSentStatus
    }
  }

  "EmailsController #email" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val controller = new EmailsController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        emailActions,
        userActions)
      val result = controller.email().apply(FakeRequest(POST, "/email")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2010-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "bcc" -> Seq(""),
          "cc" -> Seq("")))))
      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus
    }
  }

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case missing dateOf" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.email().apply(FakeRequest(POST, "/email")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "bcc" -> Seq(""),
          "cc" -> Seq("")))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case missing header" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.email().apply(FakeRequest(POST, "/email")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2010-10-10",
          "body" -> "",
          "to" -> Seq(""),
          "bcc" -> Seq(""),
          "cc" -> Seq("")))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case missing body" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.email().apply(FakeRequest(POST, "/email")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2010-10-10",
          "header" -> "",
          "to" -> Seq(""),
          "bcc" -> Seq(""),
          "cc" -> Seq("")))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "EmailsController #getEmails" should {
    "send a Ok if JSON header has a valid token" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmails(Option("")).apply(FakeRequest(GET, "/emails")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "EmailsController #getEmails" should {
    "send a Ok if JSON header has a valid token: case received" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmails(Option(EndPointReceived)).apply(FakeRequest(GET, s"/emails?status=$EndPointReceived")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "EmailsController #getEmails" should {
    "send a Ok if JSON header has a valid token: case sent" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmails(Option(EndPointSent)).apply(FakeRequest(GET, s"/emails?status=$EndPointSent")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "EmailsController #getEmails" should {
    "send a Ok if JSON header has a valid token: case trashed" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmails(Option(EndPointTrash)).apply(FakeRequest(GET, s"/emails?status=$EndPointTrash")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "EmailsController #getEmails" should {
    "send a Ok if JSON header has a valid token: case no status" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmails(None).apply(FakeRequest(GET, "/emails")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "EmailsController #getEmails" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val controller = new EmailsController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        emailActions,
        userActions)
      val result = controller.getEmails(Option("")).apply(FakeRequest(GET, "/emails?status=")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
    }
  }

  "EmailsController #getEmails" should {
    "send a BadRequest if has an invalid status" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmails(Option("NOTstatus")).apply(FakeRequest(GET, "/emails?status=NOTstatus")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe InvalidEndPointStatus

    }
  }

  "EmailsController #getEmail" should {
    "send a Ok if JSON header has a valid token" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmail("", Option("")).apply(FakeRequest(GET, s"/emails/:emailID?status=")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "EmailsController #getEmail" should {
    "send a Ok if JSON header has a valid token: case received" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmail("", Option(EndPointReceived)).apply(FakeRequest(GET, s"/emails/:emailID?status=$EndPointReceived")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "EmailsController #getEmail" should {
    "send a Ok if JSON header has a valid token: case sent" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmail("", Option(EndPointSent)).apply(FakeRequest(GET, s"/emails/:emailID?status=$EndPointSent")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "EmailsController #getEmail" should {
    "send a Ok if JSON header has a valid token: case trashed" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmail("", Option(EndPointTrash)).apply(FakeRequest(GET, s"/emails/:emailID?status=$EndPointTrash")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "EmailsController #getEmail" should {
    "send a Ok if JSON header has a valid token: case no status" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmail("", None).apply(FakeRequest(GET, "/emails/:emailID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "EmailsController #getEmail" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val controller = new EmailsController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        emailActions,
        userActions)
      val result = controller.getEmail("", Option("")).apply(FakeRequest(GET, "/emails/:emailID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
    }
  }

  "EmailsController #getEmail" should {
    "send a BadRequest if has an invalid status" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getEmail("", Option("NOTstatus")).apply(FakeRequest(GET, "/emails/:emailID?status=NOTstatus")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe InvalidEndPointStatus

    }
  }

  "EmailsController #moveInOutTrash" should {
    "send a Ok if JSON header has a valid token" in {

      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)

      val result = controller.moveInOutTrash("").apply(FakeRequest(PATCH, "/emails/:emailID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "toTrash" -> true))))

      status(result) mustBe OK
    }
  }

  "EmailsController #moveInOutTrash" should {
    "send a Forbidden if JSON header has an invalid token" in {

      val controller = new EmailsController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        emailActions,
        userActions)

      val result = controller.moveInOutTrash("").apply(FakeRequest(PATCH, "/emails/:emailID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "toTrash" -> true))))

      status(result) mustBe FORBIDDEN
    }
  }

  "EmailsController #moveInOutTrash" should {
    "send a BadRequest if JSON body has an invalid format" in {

      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)

      val result = controller.moveInOutTrash("").apply(FakeRequest(PATCH, "/emails/:emailID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "NOTtoTrash" -> true))))

      status(result) mustBe BAD_REQUEST
    }
  }

}
