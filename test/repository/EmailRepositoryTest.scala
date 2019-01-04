package repository

import java.util.UUID

import actions.EmailActions
import api.dtos.{ CreateEmailDTO, CreateUserDTO }
import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.UserMappings.{ loginTable, userTable }
import database.repository.{ ChatRepository, EmailRepository }
import org.scalatest._
import org.scalatest.tools.Durations
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }
import scala.util.Try

class EmailRepositoryTest extends WordSpec with BeforeAndAfterAll with BeforeAndAfterEach {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]

  lazy implicit val rep = new ChatRepository()
  val emailActions = new EmailRepository()

  val userCreation = new CreateUserDTO("rvalente@growin.com", "12345")
  val emailCreation = new CreateEmailDTO(
    Option(""),
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

  /** Verify if an email is inserted in database correctly*/
  "EmailRepository #insertEmail" should {
    "check if the intended email is inserted in the email table in database" in {

      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val result = Await.result(db.run(emailTable.result), Duration.Inf)

      /** Verify if the email table is not empty **/
      assert(result.nonEmpty === true)

      /** Verify if the respective arguments match **/
      assert(result.map(_.fromAddress).head === userCreation.username)
      assert(result.map(_.header).head === emailCreation.header)
      assert(result.map(_.body).head === emailCreation.body)
      assert(result.map(_.dateOf).head === emailCreation.dateOf)
      assert(result.map(_.sent).head === emailCreation.sendNow)

      /** Verify if emailID and chatID have an IIUD format **/
      assert(Try[Boolean] { UUID.fromString(result.map(_.chatID).head); true }.getOrElse(false))
      assert(Try[Boolean] { UUID.fromString(result.map(_.emailID).head); true }.getOrElse(false))
    }
  }

  /** Verify if an email is inserted in chatTable */
  "EmailRepository #insertEmail" should {
    "check if the chat parameters are inserted in the chat table in database" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val result = Await.result(db.run(chatTable.result), Duration.Inf)

      /** Verify if the chat table is not empty **/
      assert(result.nonEmpty === true)

      /** Verify if the respective arguments match **/
      assert(result.map(_.header).head === emailCreation.header)

      /** Verify if emailID and chatID have an IIUD format **/
      assert(Try[Boolean] { UUID.fromString(result.map(_.chatID).head); true }.getOrElse(false))
    }
  }

  /*
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
      */

  /** Verify if an email is inserted in database */
  "EmailRepository #insertEmail" should {
    "check if the CC parameters are inserted in the CC table in database when necessary" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val result = Await.result(db.run(ccTable.result), Duration.Inf)

      /** Verify if the cc table is**/
      emailCreation.CC match {
        case Some(_) =>
          assert(result.nonEmpty === true)
          assert(result.map(_.username).head === emailCreation.CC.get.head)

          /** Verify if ccID and emailID have an IIUD format **/
          assert(Try[Boolean] { UUID.fromString(result.map(_.CCID).head); true }.getOrElse(false))
          assert(Try[Boolean] { UUID.fromString(result.map(_.emailID).head); true }.getOrElse(false))

        case _ => assert(result.isEmpty === true)
      }
    }
  }
  /**
   * /** Verify if an email is inserted in database and respective verification of the function getEmails with status sent*/
   * "EmailRepository #getEmails" should {
   * "check if the function getEmails is able to reach the sent email inserted" in {
   * val result = emailActionsTest.getEmailsTest(userCreation.username, emailCreation, "sent")
   * assert(result === true)
   * }
   * }
   *
   * /** Verify if an email is inserted in database and respective verification of the function getEmails with status received*/
   * "EmailRepository #insertEmail" should {
   * "check if the function getEmails is able to reach the received email inserted" in {
   * val result = emailActionsTest.getEmailsTest(emailCreation.to.map(_.head).getOrElse(""), emailCreation, "received")
   * assert(result === true)
   *
   * }
   * }
   *
   * /** Verify if an email is inserted in database and respective verification of the function getEmails with status draft*/
   * "EmailRepository #insertEmail" should {
   * "check if the function getEmails is able to reach the drafted email inserted" in {
   * val result = emailActionsTest.getEmailsTest(emailCreation.to.map(_.head).getOrElse(""), emailDraftCreation, "draft")
   * assert(result === true)
   * }
   * }
   *
   * /** Verify if an email is inserted in database and respective verification of the function getEmail with status sent **/
   * "EmailRepository #insertEmail" should {
   * "check if the function getEmail is able to reach the sent email inserted" in {
   * val result = emailActionsTest.getEmailTest(userCreation.username, emailCreation, "sent")
   * assert(result === true)
   * }
   * }
   *
   * /** Verify if an email is inserted in database and respective verification of the function getEmail with status received **/
   * "EmailRepository #insertEmail" should {
   * "check if the function getEmail is able to reach the received email inserted" in {
   * val result = emailActionsTest.getEmailTest(emailCreation.to.map(_.head).getOrElse(""), emailCreation, "received")
   * assert(result === true)
   * }
   * }
   *
   * /** Verify if an email is inserted in database and respective verification of the function getEmails with status draft*/
   * "EmailRepository #insertEmail" should {
   * "check if the function getEmail is able to reach the drafted email inserted" in {
   * val result = emailActionsTest.getEmailTest(emailCreation.to.map(_.head).getOrElse(""), emailDraftCreation, "draft")
   * assert(result === true)
   * }
   * }
   *
   * //takeDraftMakeSentTest
   * /** Verify if a drafted email is inserted in database is updated to an sent email*/
   * "EmailRepository #insertEmail" should {
   * "check if the function takeDraftMakeSent is able to update the drafted email inserted" in {
   * val result = emailActionsTest.takeDraftMakeSentTest(userCreation.username, emailDraftCreation)
   * assert(result === true)
   * }
   * //takeDraftMakeSentTest(user: String, email: CreateEmailDTO)
   * }
   */
}
