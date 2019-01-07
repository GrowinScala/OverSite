package controllers
import actions.ChatActions
import api.dtos.CreateEmailDTO
import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.mappings.UserMappings._
import database.mappings._
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

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class EmailsControllerTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]
  lazy implicit val rep = new ChatRepository()
  val chatActionsTest = new ChatActions()

  val tables = Seq(chatTable, userTable, emailTable, toAddressTable, ccTable, bccTable, loginTable, shareTable)

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

  /** POST /email end-point */

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case dateOf" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "NOTdateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case header" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "NOTheader": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case body" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "NOTbody": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case sendNow" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "NOTsendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #email" should {
    "send an OK if JSON body has an valid format: case missing parameter chatID" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case missing parameter dateOf" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case missing parameter header" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case missing parameter body" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #email" should {
    "send an Ok if JSON body has a valid format: case missing parameter to" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #email" should {
    "send an Ok if JSON body has a valid format: case missing parameter BCC" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #email" should {
    "send an Ok if JSON body has a valid format: case missing parameter CC" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #email" should {
    "send a BadRequest if JSON body has an invalid format: case missing parameter sendNow" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"]
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #email" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "???")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }

  "EmailsController #email" should {
    "send a OK if JSON header has a valid token and a valid JSON body" in {
      val fakeRequest = FakeRequest(POST, s"/email")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")
        .withJsonBody(parse("""
          {
            "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "dateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow": true
          }
        """))
      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }
  /** ----------------------------------------------- */

  /** GET /emails/:status end-point */

  "EmailsController #getEmails" should {
    "send an Ok if JSON header has a valid token and status: draft" in {
      val fakeRequest = FakeRequest(GET, s"/emails/draft")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #getEmails" should {
    "send an Ok if JSON header has a valid token and status: received" in {
      val fakeRequest = FakeRequest(GET, s"/emails/received")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #getEmails" should {
    "send an Ok if JSON header has a valid token and status: sent" in {
      val fakeRequest = FakeRequest(GET, s"/emails/sent")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #getEmails" should {
    "send a BadRequest if end-point has an invalid status" in {
      val fakeRequest = FakeRequest(GET, s"/emails/:status")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #getEmails" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(GET, s"/emails/:status")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "???")

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /**  GET /emails/:status/:emailID  end-point */

  "EmailsController #getEmail" should {
    "send an Ok if JSON header has a valid token and status: draft" in {
      val fakeRequest = FakeRequest(GET, s"/emails/draft/:emailID")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #getEmail" should {
    "send an Ok if JSON header has a valid token and status: received" in {
      val fakeRequest = FakeRequest(GET, s"/emails/received/:emailID")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #getEmail" should {
    "send an Ok if JSON header has a valid token and status: sent" in {
      val fakeRequest = FakeRequest(GET, s"/emails/sent/:emailID")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #getEmail" should {
    "send a BadRequest if end-point has an invalid status" in {
      val fakeRequest = FakeRequest(GET, s"/emails/:status/:emailID")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #getEmail" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(GET, s"/emails/:status")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "???")

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

  /**  PATCH /emails/:status/:emailID  end-point */

  "EmailsController #toSent" should {
    "send an Ok if JSON header has a valid token and status: draft, and target email has to address" in {

      Await.result(db.run(emailTable += EmailRow("1ba62fff-f787-4d19-926c-1ba62fd03a9a", "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
        "pedro@hotmail.com", "2018-12-01", "Hello World!", "Have a good day Sir", false)), Duration.Inf)

      Await.result(db.run(toAddressTable += ToAddressRow(
        "4d192fff-f787-4d19-926c-1ba62fd03a9a",
        "1ba62fff-f787-4d19-926c-1ba62fd03a9a", "vfernandesgrowin.pt")), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, s"/emails/draft/1ba62fff-f787-4d19-926c-1ba62fd03a9a")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

  "EmailsController #toSent" should {
    "send a BadRequest if target email has no to address" in {

      Await.result(db.run(emailTable += EmailRow("1ba62fff-f787-4d19-926c-1ba62fd03a9a", "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
        "pedro@hotmail.com", "2018-12-01", "Hello World!", "Have a good day Sir", false)), Duration.Inf)

      val fakeRequest = FakeRequest(PATCH, s"/emails/draft/1ba62fff-f787-4d19-926c-1ba62fd03a9a")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #toSent" should {
    "send a BadRequest if end-point has an invalid status" in {
      val fakeRequest = FakeRequest(PATCH, s"/emails/:status/:emailID")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe BAD_REQUEST
    }
  }

  "EmailsController #toSent" should {
    "send a Forbidden if JSON header has an invalid token" in {
      val fakeRequest = FakeRequest(PATCH, s"/emails/:status/:emailID")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "???")

      val result = route(app, fakeRequest)
      status(result.get) mustBe FORBIDDEN
    }
  }
  /** ----------------------------------------------- */

}
