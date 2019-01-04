package repository

import actions.EmailActions
import api.dtos.{ CreateEmailDTO, CreateUserDTO }
import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.UserMappings.{ loginTable, userTable }
import database.repository.ChatRepository
import org.scalatest._
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class EmailRepositoryTest extends WordSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]

  lazy implicit val rep = new ChatRepository()
  val emailActionsTest = new EmailActions()

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

  val emailDraftCreation = new CreateEmailDTO(
    Option("123"),
    "2025-10-10",
    "Hello World",
    "This body is meant to say hello world",
    Option(Seq("pcorreia@growin.pt")),
    Option(Seq("vfernandes@growin.pt")),
    Option(Seq("joao@growin.pt")),
    false)

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

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the intended email is inserted in the email table in database" in {
      val result = emailActionsTest.insertEmailTest(userCreation.username, emailCreation)
      assert(result === true)
    }
  }

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the chatID is inserted in the chat table in database" in {
      val result = emailActionsTest.insertChatTableTest(userCreation.username, emailCreation)
      assert(result === true)
    }
  }

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the to is inserted in the toAddress table in database" in {
      val result = emailActionsTest.insertToAddressTableTest(userCreation.username, emailCreation)
      assert(result === true)
    }
  }

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the BCC is inserted in the BCC table in database" in {
      val result = emailActionsTest.insertBCCTableTest(userCreation.username, emailCreation)
      assert(result === true)
    }
  }

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the CC is inserted in the CC table in database" in {
      val result = emailActionsTest.insertCCTableTest(userCreation.username, emailCreation)
      assert(result === true)
    }
  }

  /** Verify if an email is inserted in database and respective verification of the function getEmails with status sent*/
  "EmailRepository #getEmails" should {
    "check if the function getEmails is able to reach the sent email inserted" in {
      val result = emailActionsTest.getEmailsTest(userCreation.username, emailCreation, "sent")
      assert(result === true)
    }
  }

  /** Verify if an email is inserted in database and respective verification of the function getEmails with status received*/
  "EmailRepository #insertEmail" should {
    "check if the function getEmails is able to reach the received email inserted" in {
      val result = emailActionsTest.getEmailsTest(emailCreation.to.map(_.head).getOrElse(""), emailCreation, "received")
      assert(result === true)

    }
  }

  /** Verify if an email is inserted in database and respective verification of the function getEmails with status draft*/
  "EmailRepository #insertEmail" should {
    "check if the function getEmails is able to reach the drafted email inserted" in {
      val result = emailActionsTest.getEmailsTest(emailCreation.to.map(_.head).getOrElse(""), emailDraftCreation, "draft")
      assert(result === true)
    }
  }

  /** Verify if an email is inserted in database and respective verification of the function getEmail with status sent **/
  "EmailRepository #insertEmail" should {
    "check if the function getEmail is able to reach the sent email inserted" in {
      val result = emailActionsTest.getEmailTest(userCreation.username, emailCreation, "sent")
      assert(result === true)
    }
  }

  /** Verify if an email is inserted in database and respective verification of the function getEmail with status received **/
  "EmailRepository #insertEmail" should {
    "check if the function getEmail is able to reach the received email inserted" in {
      val result = emailActionsTest.getEmailTest(emailCreation.to.map(_.head).getOrElse(""), emailCreation, "received")
      assert(result === true)
    }
  }

  /** Verify if an email is inserted in database and respective verification of the function getEmails with status draft*/
  "EmailRepository #insertEmail" should {
    "check if the function getEmail is able to reach the drafted email inserted" in {
      val result = emailActionsTest.getEmailTest(emailCreation.to.map(_.head).getOrElse(""), emailDraftCreation, "draft")
      assert(result === true)
    }
  }

  //takeDraftMakeSentTest
  /** Verify if a drafted email is inserted in database is updated to an sent email*/
  "EmailRepository #insertEmail" should {
    "check if the function takeDraftMakeSent is able to update the drafted email inserted" in {
      val result = emailActionsTest.takeDraftMakeSentTest(userCreation.username, emailDraftCreation)
      assert(result === true)
    }
    //takeDraftMakeSentTest(user: String, email: CreateEmailDTO)
  }

}
