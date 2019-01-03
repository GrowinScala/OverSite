package repository

import actions.{ ChatActions, EmailActions }
import api.dtos.{ CreateEmailDTO, CreateUserDTO }
import database.repository.ChatRepository
import org.scalatest._
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext
class ChatRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]
  lazy implicit val rep = new ChatRepository()
  val chatActionsTest = new ChatActions()

  val userCreation = new CreateUserDTO("rvalente@growin.com", "12345")
  val emailCreation = new CreateEmailDTO(
    Option("123"),
    "2025-10-10",
    "Hello World",
    "This body is meant to say hello world",
    Option(Seq("pcorreia@growin.pt")),
    Option(Seq("vfernandes@growin.pt")),
    Option(Seq("joao@growin.pt")),
    true)

  override def beforeAll() = {
    chatActionsTest.createFilesTable
  }

  override def afterAll() = {
    chatActionsTest.dropFilesTable
  }

  override def afterEach(): Unit = {
    chatActionsTest.deleteRowsTable
  }

  /** Verify if a chat is inserted in database */
  "ChatRepository #insertChat" should {
    "check if the intended chat is inserted in the chat table in database" in {
      val result = chatActionsTest.insertChatTest(userCreation.username, emailCreation)
      assert(result === true)
    }
  }

}
