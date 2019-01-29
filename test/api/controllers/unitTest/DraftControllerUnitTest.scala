package api.controllers.unitTest

import akka.stream.Materializer
import api.controllers.DraftsController
import api.validators.TokenValidator
import database.repository._
import database.repository.fake.{ FakeEmailRepositoryImpl, FakeEmailRepositoryImplWithNoDestination }
import definedStrings.ApiStrings._
import definedStrings.testStrings.ControllerStrings.{ LocalHost, TokenKey }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import UnitControllerTestsAppBuilder._

import scala.concurrent.ExecutionContext

class DraftControllerUnitTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach with Results {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy implicit val mat: Materializer = injectorWithValidToken.instanceOf[Materializer]

  val emailActions: EmailRepository = new FakeEmailRepositoryImpl()

  "DraftsController #draft" should {
    "send a Ok if JSON header has a valid token" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.draft.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))
      status(result) mustBe OK
      contentAsString(result) mustBe MailDraftStatus
    }
  }

  "DraftsController #draft" should {
    "send a Ok if JSON header has a valid token: case missing chatID" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.draft.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))
      status(result) mustBe OK
      contentAsString(result) mustBe MailDraftStatus

    }
  }

  "DraftsController #draft" should {
    "send a Ok if JSON header has a valid token: case missing to" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.draft.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))
      status(result) mustBe OK
      contentAsString(result) mustBe MailDraftStatus

    }
  }

  "DraftsController #draft" should {
    "send a Ok if JSON header has a valid token: case missing bcc" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.draft.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "CC" -> Seq("")))))
      status(result) mustBe OK
      contentAsString(result) mustBe MailDraftStatus

    }
  }

  "DraftsController #draft" should {
    "send a Ok if JSON header has a valid token: case missing cc" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.draft.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq("")))))
      status(result) mustBe OK
      contentAsString(result) mustBe MailDraftStatus

    }
  }

  "DraftsController #draft" should {
    "send a Forbidden if JSON header has a invalid token" in {
      val controller = new DraftsController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        emailActions)
      val result = controller.draft.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))
      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus
    }
  }

  "DraftsController #draft" should {
    "send a BadRequest if JSON body has an invalid format: case dateOF" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.draft.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "NOTdateOF" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "DraftsController #draft" should {
    "send a BadRequest if JSON body has an invalid format: case header" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.draft.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "NOTheader" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "DraftsController #draft" should {
    "send a BadRequest if JSON body has an invalid format: case body" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.draft.apply(FakeRequest(POST, "/draft")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "NOTbody" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "DraftsController #getDrafts" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.getDrafts(Option(true)).apply(FakeRequest(GET, "/drafts?isTrash=true")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }

  "DraftsController #getDrafts" should {
    "send a OK if JSON header has a valid token: case no TrashOption" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.getDrafts(None).apply(FakeRequest(GET, "/drafts")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }

  "DraftsController #getDrafts" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val controller = new DraftsController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        emailActions)
      val result = controller.getDrafts(Option(true)).apply(FakeRequest(GET, "/drafts?isTrash=true")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus
    }
  }

  "DraftsController #getDraft" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.getDraft("", Option(true)).apply(FakeRequest(GET, "/drafts/:draftID?isTrash=true")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }

  "DraftsController #getDraft" should {
    "send a OK if JSON header has a valid token: case no TrashOption" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.getDraft("", None).apply(FakeRequest(GET, "/drafts/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }

  "DraftsController #getDraft" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val controller = new DraftsController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        emailActions)
      val result = controller.getDraft("", Option(true)).apply(FakeRequest(GET, "/drafts/:draftID?isTrash=true")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus
    }
  }

  "DraftsController #updateDraft" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.updateDraft("").apply(FakeRequest(PUT, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))

      status(result) mustBe OK
      contentAsString(result) mustBe EmailUpdated

    }
  }

  "DraftsController #updateDraft" should {
    "send a Ok if JSON header has a valid token: case missing chatID" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.updateDraft("").apply(FakeRequest(PUT, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))

      status(result) mustBe OK
      contentAsString(result) mustBe EmailUpdated

    }
  }

  "DraftsController #updateDraft" should {
    "send a Ok if JSON header has a valid token: case missing to" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.updateDraft("").apply(FakeRequest(PUT, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))

      status(result) mustBe OK
      contentAsString(result) mustBe EmailUpdated

    }
  }

  "DraftsController #updateDraft" should {
    "send a OK if JSON header has a valid token: case missing bcc" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.updateDraft("").apply(FakeRequest(PUT, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "CC" -> Seq("")))))

      status(result) mustBe OK
      contentAsString(result) mustBe EmailUpdated

    }
  }

  "DraftsController #updateDraft" should {
    "send a OK if JSON header has a valid token: case missing cc" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.updateDraft("").apply(FakeRequest(PUT, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq("")))))

      status(result) mustBe OK
      contentAsString(result) mustBe EmailUpdated

    }
  }

  "DraftsController #updateDraft" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val controller = new DraftsController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        emailActions)
      val result = controller.updateDraft("").apply(FakeRequest(PUT, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))

      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus
    }
  }

  "DraftsController #updateDraft" should {
    "send a BadRequest if JSON body has an invalid format: case dateOf" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.updateDraft("").apply(FakeRequest(PUT, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "NOTdateOf" -> "2000-10-10",
          "header" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))

      status(result) mustBe BAD_REQUEST
    }
  }

  "DraftsController #updateDraft" should {
    "send a BadRequest if JSON body has an invalid format: case header" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.updateDraft("").apply(FakeRequest(PUT, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "NOTheader" -> "",
          "body" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))

      status(result) mustBe BAD_REQUEST
    }
  }

  "DraftsController #updateDraft" should {
    "send a BadRequest if JSON body has an invalid format: case body" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.updateDraft("").apply(FakeRequest(PUT, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "dateOf" -> "2000-10-10",
          "header" -> "",
          "NOTbody" -> "",
          "to" -> Seq(""),
          "BCC" -> Seq(""),
          "CC" -> Seq("")))))

      status(result) mustBe BAD_REQUEST
    }
  }

  "DraftsController #toSentOrDraft" should {
    "send a OK if JSON header has a valid token: case status = send" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.toSentOrDraft("").apply(FakeRequest(PATCH, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "status" -> StatusSend))))

      status(result) mustBe OK
      contentAsString(result) mustBe MailSentStatus

    }
  }

  "DraftsController #toSentOrDraft" should {
    "send a OK if JSON header has a valid token: case status = trash" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.toSentOrDraft("").apply(FakeRequest(PATCH, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "status" -> StatusTrash))))

      status(result) mustBe OK
      contentAsString(result) mustBe EmailUpdated

    }
  }

  "DraftsController #toSentOrDraft" should {
    "send a OK if JSON header has a valid token: case status = draft" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.toSentOrDraft("").apply(FakeRequest(PATCH, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "status" -> StatusDraft))))

      status(result) mustBe OK
      contentAsString(result) mustBe EmailUpdated

    }
  }

  "DraftsController #toSentOrDraft" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val controller = new DraftsController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        emailActions)
      val result = controller.toSentOrDraft("").apply(FakeRequest(PATCH, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "status" -> StatusSend))))

      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus

    }
  }

  "DraftsController #toSentOrDraft" should {
    "send a BadRequest if JSON body has an invalid status" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.toSentOrDraft("").apply(FakeRequest(PATCH, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "status" -> ""))))

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ImpossibleStatusDraft

    }
  }

  "DraftsController #toSentOrDraft" should {
    "send a BadRequest if JSON body has an missing status" in {
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActions)
      val result = controller.toSentOrDraft("").apply(FakeRequest(PATCH, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "NOTstatus" -> ""))))

      status(result) mustBe BAD_REQUEST
    }
  }

  "DraftsController #toSentOrDraft" should {
    "send a BadRequest if the email has no destination" in {
      val emailActionsWithNoDestination: EmailRepository = new FakeEmailRepositoryImplWithNoDestination()
      val controller = new DraftsController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        emailActionsWithNoDestination)
      val result = controller.toSentOrDraft("").apply(FakeRequest(PATCH, "/draft/:draftID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "status" -> StatusSend))))

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe ImpossibleToSendDraft

    }
  }

}