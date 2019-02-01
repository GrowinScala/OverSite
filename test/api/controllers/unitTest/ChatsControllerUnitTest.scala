package api.controllers.unitTest

import akka.stream.Materializer
import api.controllers.ChatController
import api.controllers.unitTest.UnitControllerTestsAppBuilder._
import api.validators.TokenValidator
import database.repository.fake.{ FakeChatRepositoryImpl, FakeUserRepositoryImpl }
import database.repository.{ ChatRepository, _ }
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

class ChatsControllerUnitTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach with Results {

  implicit private val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy implicit private val mat: Materializer = injectorWithValidToken.instanceOf[Materializer]

  private val chatActions: ChatRepository = new FakeChatRepositoryImpl()
  private val userActions: UserRepository = new FakeUserRepositoryImpl()

  "ChatController #inbox" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.inbox(Option(true)).apply(FakeRequest(GET, "/chats?isTrash=true")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "ChatController #inbox" should {
    "send a OK if JSON header has a valid token: case no TrashOption" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.inbox(None).apply(FakeRequest(GET, "/chats")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "ChatController #inbox" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.inbox(Option(true)).apply(FakeRequest(GET, "/chats?isTrash=true")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus

    }
  }

  "ChatController #getEmails" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getEmails("", Option(true)).apply(FakeRequest(GET, "/chats/:chatID/emails?isTrash=true")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "ChatController #getEmails" should {
    "send a OK if JSON header has a valid token: case no TrashOption" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getEmails("", None).apply(FakeRequest(GET, "/chats/:chatID/emails")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe OK
    }
  }

  "ChatController #getEmails" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.getEmails("", Option(true)).apply(FakeRequest(GET, "/chats/:chatID/emails?isTrash=true")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus

    }
  }

  "ChatController #moveInOutTrash" should {
    "send a Ok if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.moveInOutTrash("").apply(FakeRequest(PATCH, "/chats/:chatID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "toTrash" -> true))))
      status(result) mustBe OK
    }
  }

  "ChatController #moveInOutTrash" should {
    "send a Forbidden if JSON header has a invalid token" in {
      val controller = new ChatController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.moveInOutTrash("").apply(FakeRequest(PATCH, "/chats/:chatID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "toTrash" -> true))))
      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus

    }
  }

  "ChatController #moveInOutTrash" should {
    "send a BadRequest if JSON body has an invalid format" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.moveInOutTrash("").apply(FakeRequest(PATCH, "/chats/:chatID")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "NOTtoTrash" -> true))))
      status(result) mustBe BAD_REQUEST
    }
  }

  "ChatController #supervised" should {
    "send a Ok if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.supervised.apply(FakeRequest(POST, "/shares")
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
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.supervised().apply(FakeRequest(POST, "/shares")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "supervisor" -> ""))))
      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus

    }
  }

  "ChatController #supervised" should {
    "send a BadRequest if JSON body has an invalid format: case chatID" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.supervised.apply(FakeRequest(POST, "/shares")
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
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.supervised.apply(FakeRequest(POST, "/shares")
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
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getShares().apply(FakeRequest(GET, "/shares")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }

  "ChatController #getShares" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.getShares().apply(FakeRequest(GET, "/shares")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus

    }
  }

  "ChatController #getSharedEmails" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getSharedEmails("").apply(FakeRequest(GET, "/shares/:shareID/emails")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }

  "ChatController #getSharedEmails" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.getSharedEmails("").apply(FakeRequest(GET, "/shares/:shareID/emails")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus

    }
  }
  //TODO: REMAKE TESTS
  /*
  "ChatController #getSharedEmail" should {
    "send a OK if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.getSharedEmail("", "").apply(FakeRequest(GET, "/shares/:shareID/emails")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))

      status(result) mustBe OK
    }
  }
  */
  //TODO: REMAKE TESTS
  /*
  "ChatController #getSharedEmail" should {
    "send a Forbidden if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.getSharedEmail("", "").apply(FakeRequest(GET, "/shares/:shareID/emails")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> ""))
      status(result) mustBe FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus

    }
  }
*/
  "ChatController #takePermissions" should {
    "send a Ok if JSON header has a valid token" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.takePermissions().apply(FakeRequest(DELETE, "/shares")
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
        injectorWithInvalidToken.instanceOf[TokenValidator],
        ccWithInvalidToken,
        actorSystemWithInvalidToken,
        chatActions,
        userActions)
      val result = controller.takePermissions().apply(FakeRequest(DELETE, "/shares")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "supervisor" -> ""))))
      status(result) mustEqual FORBIDDEN
      contentAsString(result) mustBe VerifyLoginStatus

    }
  }

  "ChatController #takePermissions" should {
    "send a BadRequest if JSON body has an invalid format: case chatID" in {
      val controller = new ChatController(
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.takePermissions().apply(FakeRequest(DELETE, "/shares")
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
        injectorWithValidToken.instanceOf[TokenValidator],
        ccWithValidToken,
        actorSystemWithValidToken,
        chatActions,
        userActions)
      val result = controller.takePermissions().apply(FakeRequest(DELETE, "/shares")
        .withHeaders(CONTENT_TYPE -> JSON, HOST -> LocalHost, TokenKey -> "")
        .withBody(Json.toJson(Json.obj(
          "chatID" -> "",
          "NOTsupervisor" -> ""))))
      status(result) mustBe BAD_REQUEST
    }
  }

}