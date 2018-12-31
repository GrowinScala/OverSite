package repository

import actions.EmailActions
import api.dtos.{ CreateEmailDTO, CreateUserDTO }
import database.repository.{ ChatRepository, EmailRepository }
import org.scalatest._
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class EmailRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]

  lazy implicit val rep = new ChatRepository()
  val emailActionsTest = new EmailActions()

  val userCreation = new CreateUserDTO("rvalente@growin.com", "12345")
  val emailCreation = new CreateEmailDTO(
    None,
    "2025-10-10",
    "Hello World",
    "This body is meant to say hello world",
    Option(Seq("pcorreia@growin.pt")),
    Option(Seq("vfernandes@growin.pt")),
    Option(Seq("joao@growin.pt")),
    true)

  override def beforeAll() = {
    emailActionsTest.createFilesTable
  }

  override def afterAll() = {
    emailActionsTest.dropFilesTable
  }

  override def afterEach(): Unit = {
    emailActionsTest.deleteRowsTable
  }

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the intended email is inserted in the email table in database" in {
      emailActionsTest.insertEmailTest(userCreation, emailCreation).map(x => assert(x === true))
    }
  }
}
