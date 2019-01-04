package controllers
import actions.{ ChatActions, SupportActions }
import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import database.repository.ChatRepository
import org.scalatest.tools.Durations
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json.parse
import play.api.test.FakeRequest
import play.api.test.Helpers.{ route, status, _ }
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

class ChatsControllerTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]
  lazy implicit val rep = new ChatRepository()
  val chatActionsTest = new ChatActions()

  val tables = Seq(chatTable, userTable, emailTable, toAddressTable, ccTable, bccTable, loginTable)

  override def beforeEach(): Unit = {
    //encrypted "12345" password
    Await.result(db.run(userTable += UserRow("pedro@hotmail.com", "13012420314234138112108765216110414524878123")), Duration.Inf)
    Await.result(db.run(loginTable +=
      LoginRow("pedro@hotmail.com", "9e2907a7-b939-4b33-8899-6741e6054822", System.currentTimeMillis() + 360000, true)), Duration.Inf)
  }

  override def beforeAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.create): _*)), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.drop): _*)), Duration.Inf)
  }

  override def afterEach(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.delete): _*)), Duration.Inf)
  }

  /** GET /chats end-point */

  "ChatsController #inbox" should {
    "send an Ok if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(GET, s"/chats")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "ChatsController #inbox" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(GET, s"/chats")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "???")

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** GET /chats/:chatID/emails end-point */

  "ChatsController #getEmails" should {
    "send an Ok if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(GET, s"/chats/:chatID/emails")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "ChatsController #getEmails" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(GET, s"/chats/:chatID/emails")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "???")

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** GET /chats/:chatID/emails/:emailID end-point */

  "ChatsController #getEmail" should {
    "send an Ok if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(GET, s"/chats/:chatID/emails/:emailID")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "ChatsController #getEmail" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(GET, s"/chats/:chatID/emails/:emailID")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "???")

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /** POST /shares end-point */

  "ChatsController #supervised" should {
    "send a BadRequest if JSON body has an invalid format: case chatID" in {
      val fakeRequest = FakeRequest(POST, s"/shares")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "NOTchatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "supervisor": "pedro@hotmail.com"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController #supervised" should {
    "send a BadRequest if JSON body has an invalid format: case supervisor" in {
      val fakeRequest = FakeRequest(POST, s"/shares")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "NOTsupervisor": "pedro@hotmail.com"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController #supervised" should {
    "send a BadRequest if JSON body has an invalid format: case missing supervisor parameter" in {
      val fakeRequest = FakeRequest(POST, s"/shares")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController #supervised" should {
    "send a BadRequest if JSON body has an invalid format: case missing chatID parameter" in {
      val fakeRequest = FakeRequest(POST, s"/shares")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "supervisor": "pedro@hotmail.com"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "ChatsController #supervised" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(POST, s"/shares")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "???")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "supervisor": "pedro@hotmail.com"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  "ChatsController #supervised" should {
    "send an Ok if JSON header has a valid token and a valid JSON body" in {
      val fakeRequest = FakeRequest(POST, s"/shares")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "supervisor": "pedro@hotmail.com"
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  /** ----------------------------------------------- */
}
