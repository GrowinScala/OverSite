package controllers
import actions.{ ChatActions, SupportActions }
import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import database.repository.{ ChatRepository }
import org.scalatest.tools.Durations
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
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

  override def beforeEach() = {
    //encrypted "12345" password
    db.run(userTable += UserRow("pedro@hotmail.com", "13012420314234138112108765216110414524878123"))
    db.run(loginTable += LoginRow("pedro@hotmail.com", "9e2907a7-b939-4b33-8899-6741e6054822", System.currentTimeMillis() + 360000, true))
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

  /** Sign in end-point */
  "UsersController #inbox" should {
    "send an Ok if JSON header has a valid token" in {
      val fakeRequest = FakeRequest(GET, s"/chats")
        .withHeaders(HOST -> "localhost:9000", "Token" -> "9e2907a7-b939-4b33-8899-6741e6054822")

      val result = route(app, fakeRequest)
      status(result.get) mustBe OK
    }
  }

}
