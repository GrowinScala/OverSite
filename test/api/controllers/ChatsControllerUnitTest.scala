package api.controllers

import akka.stream.Materializer
import api.validators.TokenValidator
import database.repository.{ ChatRepository, _ }
import database.repository.fake.{ FakeChatRepositoryImpl, FakeUserRepositoryImpl }
import definedStrings.testStrings.ControllerStrings.TokenKey
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results
import play.api.test.FakeRequest
import play.api.test.Helpers._
import definedStrings.testStrings.ControllerStrings._
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

class ChatsControllerUnitTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach with Results {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy implicit val mat = UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[Materializer]

  val chatActions: ChatRepository = new FakeChatRepositoryImpl()
  val userActions: UserRepository = new FakeUserRepositoryImpl()

  "ChatController #inbox" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.inbox(Option(true)).apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "ChatController #inbox" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithInvalidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithInvalidToken,
        UnitControllerTestsAppBuilder.actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.inbox(Option(true)).apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
    }
  }

  "ChatController #getEmails" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getEmails("", Option(true)).apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "ChatController #getEmails" should {
    "send a OK if JSON header has a valid token: case no TrashOption" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getEmails("", None).apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "ChatController #getEmails" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithInvalidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithInvalidToken,
        UnitControllerTestsAppBuilder.actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.getEmails("", Option(true)).apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
    }
  }

  "ChatController #getEmail" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getEmail("", "", Option(true)).apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }

  "ChatController #getEmail" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithInvalidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithInvalidToken,
        UnitControllerTestsAppBuilder.actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.getEmail("", "", Option(true)).apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
    }
  }

  "ChatController #moveInOutTrash" should {
    "send a Ok if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.moveInOutTrash("").apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "toTrash" -> true))))
      status(result) mustBe OK
    }
  }

  "ChatController #moveInOutTrash" should {
    "send a Forbidden if JSON header has a invalid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithInvalidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithInvalidToken,
        UnitControllerTestsAppBuilder.actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.moveInOutTrash("").apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "toTrash" -> true))))
      status(result) mustBe FORBIDDEN
    }
  }

  "ChatController #moveInOutTrash" should {
    "send a BadRequest if JSON body has an invalid format" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.moveInOutTrash("").apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "NOTtoTrash" -> true))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "ChatController #supervised" should {
    "send a Ok if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.supervised.apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "supervisor" -> ""))))
      status(result) mustBe OK
    }
  }

  "ChatController #supervised" should {
    "send a Forbidden if JSON header has a invalid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithInvalidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithInvalidToken,
        UnitControllerTestsAppBuilder.actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.supervised().apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "supervisor" -> ""))))
      status(result) mustEqual FORBIDDEN
    }
  }

  "ChatController #supervised" should {
    "send a BadRequest if JSON body has an invalid format: case chatID" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.supervised.apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "NOTchatID" -> "",
          "supervisor" -> ""))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "ChatController #supervised" should {
    "send a BadRequest if JSON body has an invalid format: case supervisor" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.supervised.apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "NOTsupervisor" -> ""))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "ChatController #getShares" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getShares().apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }

  "ChatController #getShares" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithInvalidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithInvalidToken,
        UnitControllerTestsAppBuilder.actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.getShares().apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
    }
  }

  "ChatController #getSharedEmails" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getSharedEmails("").apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }

  "ChatController #getSharedEmails" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithInvalidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithInvalidToken,
        UnitControllerTestsAppBuilder.actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.getSharedEmails("").apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
    }
  }

  "ChatController #getSharedEmail" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getSharedEmail("", "").apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }

  "ChatController #getSharedEmail" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithInvalidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithInvalidToken,
        UnitControllerTestsAppBuilder.actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.getSharedEmail("", "").apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
    }
  }

  "ChatController #takePermissions" should {
    "send a Ok if JSON header has a valid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.takePermissions().apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "supervisor" -> ""))))
      status(result) mustBe OK
    }
  }

  "ChatController #takePermissions" should {
    "send a Forbidden if JSON header has a invalid token" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithInvalidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithInvalidToken,
        UnitControllerTestsAppBuilder.actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.takePermissions().apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "supervisor" -> ""))))
      status(result) mustEqual FORBIDDEN
    }
  }

  "ChatController #takePermissions" should {
    "send a BadRequest if JSON body has an invalid format: case chatID" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.takePermissions().apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "NOTchatID" -> "",
          "supervisor" -> ""))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "ChatController #takePermissions" should {
    "send a BadRequest if JSON body has an invalid format: case supervisor" in {
      val controller = new ChatController(
        UnitControllerTestsAppBuilder.injectorWithValidToken.instanceOf[TokenValidator],
        UnitControllerTestsAppBuilder.ccWithValidToken,
        UnitControllerTestsAppBuilder.actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.takePermissions().apply(FakeRequest()
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "NOTsupervisor" -> ""))))
      status(result) mustBe BAD_REQUEST
    }
  }

}