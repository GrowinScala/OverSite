package repository

import api.dtos.{ CreateEmailDTO, CreateUserDTO }
import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.UserMappings.{ loginTable, userTable }
import database.repository.{ ChatRepository, ChatRepositoryImpl, EmailRepositoryImpl }
import org.scalatest._
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }
class ChatRepositoryTest extends WordSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]
  lazy implicit val rep = new ChatRepositoryImpl()

  val emailActions = new EmailRepositoryImpl()
  val chatActions = new ChatRepositoryImpl()

  val userCreation = new CreateUserDTO("rvalente@growin.com", "12345")
  val userCreationWrongUser = new CreateUserDTO("pluis@growin.com", "12345")
  val emailCreation = new CreateEmailDTO(
    Option("123"),
    "2025-10-10",
    "Hello World",
    "This body is meant to say hello world",
    Option(Seq("pcorreia@growin.pt")),
    Option(Seq("vfernandes@growin.pt")),
    Option(Seq("joao@growin.pt")),
    true)

  val tables = Seq(chatTable, userTable, emailTable, toAddressTable, ccTable, bccTable, loginTable)

  override def beforeAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.create): _*)), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.drop): _*)), Duration.Inf)
  }

  override def afterEach(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.delete): _*)), Duration.Inf)
  }

  /* Verify if a chat is inserted in database */
  "ChatRepository #insertChat" should {
    "check if the chat is inserted in the chat table correctly" in {
      val resultchatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      Await.result(chatActions.insertChat(emailCreation, resultchatID), Duration.Inf)
      val resultChatTable = Await.result(db.run(chatTable.result), Duration.Inf)
      /** Verify if something was inserted in the chat table*/
      assert(resultChatTable.nonEmpty)

      /** Verify if the chatID in chat Table matches with email inserted */
      resultChatTable.map(row => assert(row.chatID === resultchatID))

      /** Verify if the header in chat Table matches with email inserted */
      resultChatTable.map(row => assert(row.header === emailCreation.header))
    }
  }

  /* Verify if a chat is inserted in database */
  "ChatRepository #getInbox" should {
    "check if the Inbox has the right messages for an specific user" in {
      val resultchatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultInbox = Await.result(chatActions.getInbox(userCreation.username), Duration.Inf)

      /** Verify if Inbox is not empty*/
      assert(resultInbox.nonEmpty)

      /** Verify if the chatID in chat Table matches with email inserted */
      resultInbox.map(row => assert(row._1 === resultchatID))

      /** Verify if the header in chat Table matches with email inserted */
      resultInbox.map(row => assert(row._2 === emailCreation.header))
    }
  }

  /* Verify if a chat is inserted in database */
  "ChatRepository #getInbox" should {
    "check if the Inbox is empty for an user without messages" in {
      val resultchatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultInbox = Await.result(chatActions.getInbox(userCreationWrongUser.username), Duration.Inf)

      /** Verify if Inbox is not empty*/
      assert(resultInbox.isEmpty)
    }
  }

}