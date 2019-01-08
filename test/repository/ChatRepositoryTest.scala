package repository

import api.dtos.{ CreateEmailDTO, CreateShareDTO, CreateUserDTO }
import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.mappings.UserMappings._
import database.repository.{ ChatRepository, ChatRepositoryImpl, EmailRepositoryImpl }
import org.scalatest._
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
class ChatRepositoryTest extends WordSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]
  lazy implicit val rep: ChatRepositoryImpl = new ChatRepositoryImpl()

  val emailActions = new EmailRepositoryImpl()
  val chatActions = new ChatRepositoryImpl()

  val userCreation = new CreateUserDTO("rvalente@growin.com", "12345")
  val userCreationWrongUser = new CreateUserDTO("pluis@growin.com", "12345")
  val emailCreation = new CreateEmailDTO(
    Option("123"),
    "2025-10-10",
    "Hello World",
    "This body is meant to say hello world",

    /** To is an obligated parameter, since chats will not work with drafts */
    Option(Seq("pcorreia@growin.pt")),
    Option(Seq("vfernandes@growin.pt")),
    Option(Seq("joao@growin.pt")),
    true)

  val tables = Seq(chatTable, userTable, emailTable, toAddressTable, ccTable, bccTable, loginTable, shareTable)

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
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      Await.result(chatActions.insertChat(emailCreation, resultChatID), Duration.Inf)
      val resultChatTable = Await.result(db.run(chatTable.result), Duration.Inf)

      /** Verify if something was inserted in the chat table */
      assert(resultChatTable.nonEmpty)

      /** Verify if the chatID in chat Table matches with email inserted */
      resultChatTable.map(row => assert(row.chatID === resultChatID))

      /** Verify if the header in chat Table matches with email inserted */
      resultChatTable.map(row => assert(row.header === emailCreation.header))
    }
  }

  /* Verify if a chat is inserted in database */
  "ChatRepository #getInbox" should {
    "check if the Inbox has the right messages for an specific user" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultInbox = Await.result(chatActions.getInbox(userCreation.username), Duration.Inf)

      /** Verify if Inbox is not empty */
      assert(resultInbox.nonEmpty)

      /** Verify if the chatID in chat Table matches with email inserted */
      resultInbox.map(row => assert(row.Id === resultChatID))

      /** Verify if the header in chat Table matches with email inserted */
      resultInbox.map(row => assert(row.header === emailCreation.header))
    }
  }

  /* Verify if a chat is inserted in database */
  "ChatRepository #getInbox" should {
    "check if the Inbox is empty for an user without messages" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultInbox = Await.result(chatActions.getInbox(userCreationWrongUser.username), Duration.Inf)

      /** Verify if Inbox is not empty */
      assert(resultInbox.isEmpty)
    }
  }

  /* Verify if a the function getEmails selects the right emails through userName and chatID */
  "ChatRepository #getEmails" should {
    "check if the email is returned properly when chatID and username are provided" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultGet = Await.result(chatActions.getEmails(userCreation.username, resultChatID), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)

      /** Verify if Inbox is not empty */
      assert(resultGet.nonEmpty)

      /** Verify if the chatID in chat Table matches with email inserted */
      resultGet.map(row => assert(row.Id === resultEmailTable.map(_.emailID).head))

      /** Verify if the header in chat Table matches with email inserted */
      resultGet.map(row => assert(row.header === emailCreation.header))
    }
  }

  /* Verify if a the function getEmails selects no emails through wrong userName and chatID */
  "ChatRepository #getEmails" should {
    "check if the email is returned properly when chatID and wrong username are provided" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultGet = Await.result(chatActions.getEmails(userCreationWrongUser.username, resultChatID), Duration.Inf)

      /** Verify if Inbox is empty */
      assert(resultGet.isEmpty)
    }
  }

  /* Verify if a the function getEmails selects no emails through wrong userName and chatID */
  "ChatRepository #getEmails" should {
    "check if the email is returned properly when wrong chatID and username are provided" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultGet = Await.result(chatActions.getEmails(userCreation.username, "0000"), Duration.Inf)

      /** Verify if Inbox is empty */
      assert(resultGet.isEmpty)
    }
  }

  /* Verify if a the function getEmail selects the right email through userName and chatID */
  "ChatRepository #getEmail" should {
    "check if the email is returned properly when chatID, emailID and username are provided" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)
      val resultGet = Await.result(chatActions.getEmail(userCreation.username, resultChatID, resultEmailTable.map(_.emailID).head), Duration.Inf)

      /** Verify if resultGet is not empty */
      assert(resultGet.nonEmpty)

      /** Verify if the parameters of getEmail return match */
      resultGet.map(row => assert(row.fromAddress === resultEmailTable.map(_.fromAddress).head))

      /** Verify the "Tos" of resultGet and the ones provided in the emailCreation */
      val resultTosCompare = resultGet.map(row => row.username).zip(emailCreation.to.getOrElse(Seq()))
      resultTosCompare.map(row => assert(row._1 === row._2))

      resultGet.map(row => assert(row.header === resultEmailTable.map(_.header).head))

      resultGet.map(row => assert(row.body === resultEmailTable.map(_.body).head))

      resultGet.map(row => assert(row.dateOf === resultEmailTable.map(_.dateOf).head))

    }
  }

  /* Verify if a the function getEmail selects no emails through wrong userName and chatID */
  "ChatRepository #getEmail" should {
    "check if the email is no returned when chatID, emailID and wrong username are provided" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)
      val resultGet = Await.result(chatActions.getEmail(userCreationWrongUser.username, resultChatID, resultEmailTable.map(_.emailID).head), Duration.Inf)

      /** Verify if resultGet is not empty */
      assert(resultGet.isEmpty)
    }
  }

  /* Verify if a the function getEmail selects no emails through  userName and wrong chatID */
  "ChatRepository #getEmail" should {
    "check if the email is no returned when wrong chatID, emailID and username are provided" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)
      val resultGet = Await.result(chatActions.getEmail(userCreation.username, "0000", resultEmailTable.map(_.emailID).head), Duration.Inf)

      /** Verify if resultGet is not empty */
      assert(resultGet.isEmpty)
    }
  }

  /* Verify if a the function getEmail selects no emails through  userName and wrong chatID */
  "ChatRepository #getEmail" should {
    "check if the email is no returned when chatID, wrong emailID and username are provided" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      Await.result(db.run(emailTable.result), Duration.Inf)
      val resultGet = Await.result(chatActions.getEmail(userCreation.username, resultChatID, "0000"), Duration.Inf)

      /** Verify if resultGet is not empty */
      assert(resultGet.isEmpty)
    }
  }

  /* Verify if a the function insertPermission inserts the permission correctly */
  "ChatRepository #insertPermission" should {
    "check if the permission to some chat is proceeded correctly" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)

      val shareCreation = new CreateShareDTO(resultChatID, "mreis@growin.pt")

      val resultShareID = Await.result(chatActions.insertPermission(userCreation.username, shareCreation), Duration.Inf)

      val resultShareTable = Await.result(db.run(shareTable.result), Duration.Inf)

      /** Verify if resultShareTable is not empty */
      assert(resultShareTable.nonEmpty)

      /** verify if the parameters of the sharTeable are correct */
      resultShareTable.map(row => assert(row.chatID === resultChatID))

      resultShareTable.map(row => assert(row.fromUser === userCreation.username))

      resultShareTable.map(row => assert(row.shareID === resultShareID))

      resultShareTable.map(row => assert(row.toUser === shareCreation.supervisor))
    }
  }

  /* Verify if getShares returns the permissions correctly */
  "ChatRepository #getShares" should {
    "check if the emails that were allowed to supervise are returned correctly" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)

      val shareCreation = new CreateShareDTO(resultChatID, "mreis@growin.pt")

      Await.result(chatActions.insertPermission(userCreation.username, shareCreation), Duration.Inf)

      val returnShares = Await.result(chatActions.getShares(shareCreation.supervisor), Duration.Inf)

      /** Verify if returnShares is not empty */
      assert(returnShares.nonEmpty)

      /** Verify if the parameeters returned are correct */
      returnShares.map(row => assert(row.Id === resultChatID))

      returnShares.map(row => assert(row.header === emailCreation.header))
    }
  }

  /* Verify if getShares doesn't return the permissions to other user */
  "ChatRepository #getShares" should {
    "check if the emails that were allowed to supervise are not returned for other user" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)

      val shareCreation = new CreateShareDTO(resultChatID, "mreis@growin.pt")

      Await.result(chatActions.insertPermission(userCreation.username, shareCreation), Duration.Inf)

      val returnShares = Await.result(chatActions.getShares("jproenca@growin.pt"), Duration.Inf)

      /** Verify if returnShares is not empty */
      assert(returnShares.isEmpty)
    }
  }

  /* Verify if getSharedEmails returns the permission emails correctly */
  "ChatRepository #getSharedEmails" should {
    "check if the emails that were allowed to supervise are returned correctly" in {

      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)

      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)

      val resultEmailID = resultEmailTable.map(row => row.emailID).head

      val shareCreation = new CreateShareDTO(resultChatID, "mreis@growin.pt")

      val resultShareID = Await.result(chatActions.insertPermission(userCreation.username, shareCreation), Duration.Inf)

      val returnShares = Await.result(chatActions.getSharedEmails(shareCreation.supervisor, resultShareID), Duration.Inf)

      /** Verify if returnShares is not empty */
      assert(returnShares.nonEmpty)

      /** Verify if the parameters returned are correct */
      returnShares.map(row => assert(row.Id === resultEmailID))
      returnShares.map(row => assert(row.header === emailCreation.header))

    }
  }

  /* Verify if getSharedEmails doesnt return the permission emails for an unauthorized user */
  "ChatRepository #getSharedEmails" should {
    "check if the emails that were allowed to supervise are not returned for other user" in {

      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)

      Await.result(db.run(emailTable.result), Duration.Inf)

      val shareCreation = new CreateShareDTO(resultChatID, "mreis@growin.pt")

      val resultShareID = Await.result(chatActions.insertPermission(userCreation.username, shareCreation), Duration.Inf)

      val returnShares = Await.result(chatActions.getSharedEmails("jproenca@growin.pt", resultShareID), Duration.Inf)

      /** Verify if returnShares is not empty */
      assert(returnShares.isEmpty)
    }
  }

  // def getSharedEmail(userEmail: String, shareID: String, emailID: String): Future[Seq[(String, String, String, String, String, String)]] = {

  /* Verify if getSharedEmail return a specific email correctly */
  "ChatRepository #getSharedEmail" should {
    "check if a specific email that was allowed to supervise is returned correctly" in {

      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)

      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)

      val resultEmailID = resultEmailTable.map(row => row.emailID).head

      val shareCreation = new CreateShareDTO(resultChatID, "mreis@growin.pt")

      val resultShareID = Await.result(chatActions.insertPermission(userCreation.username, shareCreation), Duration.Inf)

      val returnShare = Await.result(chatActions.getSharedEmail(shareCreation.supervisor, resultShareID, resultEmailID), Duration.Inf)

      /** Verify if returnShares is not empty */
      assert(returnShare.nonEmpty)

      /** Verify if the parameters returned are correct */
      returnShare.map(row => assert(row.chatID === resultChatID))

      returnShare.map(row => assert(row.fromAddress === userCreation.username))

      returnShare.map(row => assert(row.username === emailCreation.to.getOrElse(Seq()).head))

      returnShare.map(row => assert(row.header === emailCreation.header))

      returnShare.map(row => assert(row.body === emailCreation.body))

      returnShare.map(row => assert(row.dateOf === emailCreation.dateOf))
    }
  }

  /* Verify if getSharedEmail doesnt return a specific email to the wrong user */
  "ChatRepository #getSharedEmail" should {
    "check if a specific email that was allowed to supervise is not accessed by the wrong user" in {

      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)
      val resultEmailID = resultEmailTable.map(row => row.emailID).head
      val shareCreation = new CreateShareDTO(resultChatID, "mreis@growin.pt")
      val resultShareID = Await.result(chatActions.insertPermission(userCreation.username, shareCreation), Duration.Inf)
      val returnShare = Await.result(chatActions.getSharedEmail("jproenca@growin.pt", resultShareID, resultEmailID), Duration.Inf)

      /** Verify if returnShares is not empty */
      assert(returnShare.isEmpty)
    }
  }
  /* NOT WORKING THANKS TO SLICK BUG
  /* Verify if deletePermission takes the permission from the supervised user*/
  "ChatRepository #deletePermission" should {
    "check if a an user is not allowed to access the emails anymore after permission deleted" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)
      val resultEmailID = resultEmailTable.map(row => row.emailID).head
      val shareCreation = new CreateShareDTO(resultChatID, "mreis@growin.pt")
      val resultShareID = Await.result(chatActions.insertPermission(userCreation.username, shareCreation), Duration.Inf)
      Await.result(chatActions.deletePermission(userCreation.username, shareCreation.supervisor, resultChatID), Duration.Inf)
      val returnShares = Await.result(chatActions.getSharedEmails(shareCreation.supervisor, resultShareID), Duration.Inf)
      /** Verify if returnShares is not empty */
      assert(returnShares.isEmpty)
    }
  }
*/
}
