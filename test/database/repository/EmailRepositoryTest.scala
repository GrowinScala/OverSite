/*package database.repository

import api.dtos.CreateEmailDTO
import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import org.scalatest._
import org.scalatest.Matchers
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext
import scala.util.Success

class EmailRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  val testDb: Database = injector.instanceOf[Database]

  val chatRepository: ChatRepositoryImpl = new ChatRepositoryImpl()(executionContext = ec, db = testDb)
  val emailsRepository: EmailRepository = new EmailRepositoryImpl()(executionContext = ec, db = testDb, chatActions = chatRepository)

  val tables = Seq(chatTable, emailTable, toAddressTable, ccTable, bccTable)

  override def beforeAll(): Unit = {
    testDb.run(DBIO.seq(tables.map(_.schema.create): _*))
  }

  override def afterAll(): Unit = {
    testDb.run(DBIO.seq(tables.map(_.schema.drop): _*))
  }

  override def afterEach(): Unit = {
    testDb.run(DBIO.seq(tables.map(_.delete): _*))
  }

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the intended email is inserted in the email table in database" in {
      val username: String = "rvalente@growin.com"
      val email: CreateEmailDTO = CreateEmailDTO(
        Option("123"),
        "2025-10-10",
        "Hello World",
        "This body is meant to say hello world",
        Option(Seq("pcorreia@growin.pt")),
        Option(Seq("vfernandes@growin.pt")),
        Option(Seq("joao@growin.pt")),
        true)

      val result = emailsRepository.insertEmail(username, email)
      result.map { chatId =>
        println(chatId)
        assert(true)
      }

      /*
      val userCreation = new CreateUserDTO("rvalente@growin.com", "12345")
  val emailCreation =
       */
    }
  }

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the chatID is inserted in the chat table in database" in {
      assert(true === true)
    }
  }

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the to is inserted in the toAddress table in database" in {
      assert(true === true)
    }
  }

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the BCC is inserted in the BCC table in database" in {
      assert(true === true)
    }
  }

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the CC is inserted in the CC table in database" in {
      assert(true === true)
    }
  }
}
*/ 