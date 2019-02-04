package api.controllers.unitTest

import akka.stream.Materializer
import api.controllers.EmailsController
import api.controllers.unitTest.UnitControllerTestsAppBuilder._
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

import scala.concurrent.ExecutionContext

class EmailsControllerUnitTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach with Results {

  implicit private val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy implicit private val mat: Materializer = UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[Materializer]

  private val emailActions: EmailRepository = new FakeEmailRepositoryImpl()
  private val userActions: UserRepository = new FakeUserRepositoryImpl()

  "EmailsController #email" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.email().apply(FakeRequest(POST, "/email")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "chatID" -> EmptyString,
          "dateOf" -> "2010-10-10",
          "header" -> EmptyString,
          "body" -> EmptyString,
          "to" -> Seq(EmptyString),
          "bcc" -> Seq(EmptyString),
          "cc" -> Seq(EmptyString)))))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "dateOf" -> "2010-10-10",
          "header" -> EmptyString,
          "body" -> EmptyString,
          "to" -> Seq(EmptyString),
          "bcc" -> Seq(EmptyString),
          "cc" -> Seq(EmptyString)))))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "chatID" -> EmptyString,
          "dateOf" -> "2010-10-10",
          "header" -> EmptyString,
          "body" -> EmptyString,
          "bcc" -> Seq(EmptyString),
          "cc" -> Seq(EmptyString)))))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "chatID" -> EmptyString,
          "dateOf" -> "2010-10-10",
          "header" -> EmptyString,
          "body" -> EmptyString,
          "to" -> Seq(EmptyString),
          "cc" -> Seq(EmptyString)))))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "chatID" -> EmptyString,
          "dateOf" -> "2010-10-10",
          "header" -> EmptyString,
          "body" -> EmptyString,
          "to" -> Seq(EmptyString),
          "bcc" -> Seq(EmptyString)))))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "chatID" -> EmptyString,
          "dateOf" -> "2010-10-10",
          "header" -> EmptyString,
          "body" -> EmptyString,
          "to" -> Seq(EmptyString),
          "bcc" -> Seq(EmptyString),
          "cc" -> Seq(EmptyString)))))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "chatID" -> EmptyString,
          "header" -> EmptyString,
          "body" -> EmptyString,
          "to" -> Seq(EmptyString),
          "bcc" -> Seq(EmptyString),
          "cc" -> Seq(EmptyString)))))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "chatID" -> EmptyString,
          "dateOf" -> "2010-10-10",
          "body" -> EmptyString,
          "to" -> Seq(EmptyString),
          "bcc" -> Seq(EmptyString),
          "cc" -> Seq(EmptyString)))))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "chatID" -> EmptyString,
          "dateOf" -> "2010-10-10",
          "header" -> EmptyString,
          "to" -> Seq(EmptyString),
          "bcc" -> Seq(EmptyString),
          "cc" -> Seq(EmptyString)))))
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
      val result = controller.getEmails(Option(EmptyString)).apply(FakeRequest(GET, "/emails")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
      val result = controller.getEmails(Option(EmptyString)).apply(FakeRequest(GET, "/emails?status=")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
      val result = controller.getEmail(emailID = EmptyString, Option(EmptyString)).apply(FakeRequest(GET, s"/emails/:emailID?status=")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
      val result = controller.getEmail(emailID = EmptyString, Option(EndPointReceived)).apply(FakeRequest(GET, s"/emails/:emailID?status=$EndPointReceived")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
      val result = controller.getEmail(emailID = EmptyString, Option(EndPointSent)).apply(FakeRequest(GET, s"/emails/:emailID?status=$EndPointSent")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
      val result = controller.getEmail(emailID = EmptyString, Option(EndPointTrash)).apply(FakeRequest(GET, s"/emails/:emailID?status=$EndPointTrash")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
      val result = controller.getEmail(emailID = EmptyString, None).apply(FakeRequest(GET, "/emails/:emailID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
      val result = controller.getEmail(emailID = EmptyString, Option(EmptyString)).apply(FakeRequest(GET, "/emails/:emailID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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
      val result = controller.getEmail(emailID = EmptyString, Option("NOTstatus")).apply(FakeRequest(GET, "/emails/:emailID?status=NOTstatus")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
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

      val result = controller.moveInOutTrash(emailID = EmptyString).apply(FakeRequest(PATCH, "/emails/:emailID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "toTrash" -> true))))

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

      val result = controller.moveInOutTrash(emailID = EmptyString).apply(FakeRequest(PATCH, "/emails/:emailID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "toTrash" -> true))))

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

      val result = controller.moveInOutTrash(emailID = EmptyString).apply(FakeRequest(PATCH, "/emails/:emailID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString)
        .withBody(Json.toJson(Json.obj(
          fields = "NOTtoTrash" -> true))))

      status(result) mustBe BAD_REQUEST
    }
  }

  "EmailsController #getSharedEmail" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new EmailsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions,
        userActions)
      val result = controller.getSharedEmail(shareID = EmptyString, emailID = EmptyString).apply(FakeRequest(GET, "/shares/:shareID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))

      status(result) mustBe OK
    }
  }

  "EmailsController #getSharedEmail" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new EmailsController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        emailActions,
        userActions)
      val result = controller.getSharedEmail(shareID = EmptyString, emailID = EmptyString).apply(FakeRequest(GET, "/shares/:shareID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> EmptyString))
      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus

    }
  }

}
