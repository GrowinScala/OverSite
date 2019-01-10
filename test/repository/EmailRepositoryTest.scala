package repository

import java.util.UUID

import api.dtos.{ CreateEmailDTO, CreateUserDTO, EmailInfoDTO, EmailMinimalInfoDTO }
import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.UserMappings.{ loginTable, userTable }
import database.repository.{ ChatRepositoryImpl, EmailRepositoryImpl }
import definedStrings.testStrings.RepositoryStrings._
import generators._
import org.scalatest._
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

  lazy implicit val rep: ChatRepositoryImpl = new ChatRepositoryImpl()
  val emailActions = new EmailRepositoryImpl()

  val userGenerator = new Generator()
  val userCreation = new CreateUserDTO(userGenerator.username, userGenerator.password)

  val defaultCreation = new Generator()
  val emailCreation = new CreateEmailDTO(
    Option(defaultCreation.ID),
    defaultCreation.dateOf,
    defaultCreation.header,
    defaultCreation.body,
    Option(new Generator().emailAddresses),
    Option(new Generator().emailAddresses),
    Option(new Generator().emailAddresses),
    true)

  val DraftCreation = new Generator()
  val emailDraftCreation = new CreateEmailDTO(
    Option(DraftCreation.ID),
    DraftCreation.dateOf,
    DraftCreation.header,
    DraftCreation.body,
    Option(new Generator().emailAddresses),
    Option(new Generator().emailAddresses),
    Option(new Generator().emailAddresses),
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
  EmailRepository + InsertEmailFunction should {
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
      assert(Try[Boolean] {
        UUID.fromString(result.map(_.chatID).head)
        true
      }
        .getOrElse(false))

      assert(Try[Boolean] {
        UUID.fromString(result.map(_.emailID).head)
        true
      }
        .getOrElse(false))
    }
  }

  /** Verify if an email is inserted in chatTable correctly*/
  EmailRepository + InsertEmailFunction should {
    "check if the chat parameters are inserted in the chat table in database" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val result = Await.result(db.run(chatTable.result), Duration.Inf)

      /** Verify if the chat table is not empty **/
      assert(result.nonEmpty === true)

      /** Verify if the respective arguments match **/
      assert(result.map(_.header).head === emailCreation.header)

      /** Verify if chatID have an IIUD format **/
      assert(Try[Boolean] {
        UUID.fromString(result.map(_.chatID).head)
        true
      }
        .getOrElse(false))
    }
  }

  /** Verify if an email is inserted in the toAddress table correctly */
  EmailRepository + InsertEmailFunction should {
    "check if the to is inserted in the toAddress table in database" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultToAddresstable = Await.result(db.run(toAddressTable.result), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)

      emailCreation.to match {

        case Some(x) if x.nonEmpty =>
          /** If the parameter TO exists it is verified if the toAddress table is not empty*/
          assert(resultToAddresstable.nonEmpty === true)

          /** Verify if the username of toAddress table is the same as toAddress parameter of email inserted*/
          assert(resultToAddresstable.map(_.username) === emailCreation.to.get)

          /** Verify if the sequence of toID have an IIUD format **/
          assert(Try[Boolean] {
            resultToAddresstable.map(row =>
              UUID.fromString(row.toID))
            true
          }
            .getOrElse(false))

          /** Verify if the sequence of emailID have an IIUD format **/
          assert(Try[Boolean] {
            UUID.fromString(resultToAddresstable.map(_.emailID).head)
            true
          }
            .getOrElse(false))

          /** Verify if emailID of email table is the same as toAddress table */
          resultEmailTable.map(row =>
            resultToAddresstable.map(rowTo =>
              assert(rowTo.emailID === row.emailID)))

        case _ =>
          /** If the parameter TO does not exists it is verified if the CC table is empty*/
          assert(resultToAddresstable.isEmpty === true)
      }
    }
  }

  /** Verify if an email is inserted in BCC table correctly*/
  EmailRepository + InsertEmailFunction should {
    "check if the BCC is inserted in the BCC table in database" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultBCCtable = Await.result(db.run(bccTable.result), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)

      /** Verify if the bcc table is created only if necessary**/
      emailCreation.BCC match {
        case Some(_) =>

          /** If the parameter CC exists it is verified if the CC table is not empty*/
          assert(resultBCCtable.nonEmpty === true)

          /** Verify if the username of CC table is the same as CC parameter of email inserted*/
          assert(resultBCCtable.map(_.username) === emailCreation.BCC.get)

          /** Verify if sequence of ccIDs have an UUID format **/
          assert(Try[Boolean] {
            resultBCCtable.map(row => UUID.fromString(row.BCCID))
            true
          }
            .getOrElse(false))

          /** Verify if the sequence of emailID have an IIUD format **/
          assert(Try[Boolean] {
            UUID.fromString(resultBCCtable.map(_.emailID).head)
            true
          }
            .getOrElse(false))

          /** Verify if emailID of email table is the same as CC table */
          resultEmailTable.map(row =>
            resultBCCtable.map(rowBCC =>
              assert(rowBCC.emailID === row.emailID)))

        case _ =>

          /** If the parameter CC does not exists it is verified if the CC table is empty*/
          assert(resultBCCtable.isEmpty === true)
      }
    }
  }

  /** Verify if an email is inserted in CC table correctly*/
  EmailRepository + InsertEmailFunction should {
    "check if the CC parameters are inserted in the CC table in database when necessary" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultCCtable = Await.result(db.run(ccTable.result), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)
      /** Verify if the cc table is created only if necessary**/
      emailCreation.CC match {
        case Some(_) =>

          /** If the parameter CC exists it is verified if the CC table is not empty*/
          assert(resultCCtable.nonEmpty === true)

          /** Verify if the username of CC table is the same as CC parameter of email inserted*/
          assert(resultCCtable.map(_.username) === emailCreation.CC.get)

          /** Verify if sequence of ccIDs have an IIUD format **/
          assert(Try[Boolean] {
            resultCCtable.map(row =>
              UUID.fromString(row.CCID))
            true
          }
            .getOrElse(false))

          /** Verify if the sequence of emailID have an IIUD format **/
          assert(Try[Boolean] {
            UUID.fromString(resultCCtable.map(_.emailID).head)
            true
          }
            .getOrElse(false))

          /** Verify if emailID of email table is the same as CC table */
          resultEmailTable.map(row =>
            resultCCtable.map(rowCC =>
              assert(rowCC.emailID === row.emailID)))

        case _ =>

          /** If the parameter CC does not exists it is verified if the CC table is empty*/
          assert(resultCCtable.isEmpty === true)
      }
    }
  }

  /** Verify the function getEmails */
  EmailRepository + GetEmailsFunction should {
    "check if the function getEmails is able to reach the email inserted" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)
      /** getEmails for sent and received cases */
      if (emailCreation.sendNow) {
        val resultSent = Await.result(emailActions.getEmails(userCreation.username, StatusSent), Duration.Inf)
        val resultReceived = emailCreation.to.get.map(row =>
          Await.result(emailActions.getEmails(row, StatusReceived), Duration.Inf))

        assert(resultSent === resultEmailTable.map(row =>
          EmailMinimalInfoDTO(row.emailID, row.header)))

        assert(resultReceived === emailCreation.to.get.map(_ =>
          resultEmailTable.map(row => EmailMinimalInfoDTO(row.emailID, row.header))))
      } else {
        /** getEmails for drafted cases */
        val resultDraft = Await.result(emailActions.getEmails(userCreation.username, StatusDraft), Duration.Inf)
        assert(resultDraft === resultEmailTable.map(row =>
          EmailMinimalInfoDTO(row.emailID, row.header)))
      }
    }
  }

  /** Verify the function getEmail **/
  EmailRepository + GetEmailFunction should {
    "check if the function getEmail is able to reach the email inserted" in {
      Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)
      if (emailCreation.sendNow) {
        val resultSent = Await.result(emailActions.getEmail(userCreation.username, StatusSent, resultEmailTable.map(_.emailID).head), Duration.Inf)
        val resultReceived = emailCreation.to.get.map(row =>
          Await.result(
            emailActions.getEmail(row, StatusReceived, resultEmailTable.map(_.emailID).head), Duration.Inf))

        val resultTos = emailCreation.to.get.map(to => EmailInfoDTO(
          resultEmailTable.head.chatID,
          resultEmailTable.head.fromAddress,
          to,
          resultEmailTable.head.header,
          resultEmailTable.head.body,
          resultEmailTable.head.dateOf))
        assert(resultSent === resultTos)
        assert(resultReceived === emailCreation.to.get.map(_ => resultTos))
      } else {
        /** getEmail for drafted cases */
        val resultDraft = Await.result(emailActions.getEmail(userCreation.username, StatusDraft, resultEmailTable.map(_.emailID).head), Duration.Inf)

        emailCreation.to match {
          /** If the parameter TO exists*/
          case Some(x) if x.nonEmpty =>
            assert(resultDraft === emailCreation.to.get.map(to => (
              resultEmailTable.head.chatID,
              resultEmailTable.head.fromAddress,
              to,
              resultEmailTable.head.header,
              resultEmailTable.head.body,
              resultEmailTable.head.dateOf)))
          /** In case there are no TO parameters in email*/
          case _ =>
            assert(resultDraft.head === EmailInfoDTO(
              resultEmailTable.head.chatID,
              resultEmailTable.head.fromAddress,
              "",
              resultEmailTable.head.header,
              resultEmailTable.head.body,
              resultEmailTable.head.dateOf))
        }
      }
    }

    /** Verify if a drafted email is inserted in database is updated to an sent email*/
    EmailRepository + TakeDraftMakeSent should {
      "check if the function takeDraftMakeSent is able to update the drafted email inserted" in {
        Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
        val resultEmailID = Await.result(emailActions.getEmails(userCreation.username, StatusDraft), Duration.Inf)

        /** Verify if there is any drafted email */
        if (resultEmailID.nonEmpty) {
          Await.result(emailActions.takeDraftMakeSent(userCreation.username, resultEmailID.head.Id), Duration.Inf)
          val resultEmailIDNew = Await.result(emailActions.getEmails(userCreation.username, StatusDraft), Duration.Inf)

          if (emailCreation.to.get.nonEmpty)
            assert(resultEmailIDNew.isEmpty)

          /** In case there are no parameters in "to" field */
          else assert(resultEmailIDNew.nonEmpty)
        } else assert(true === true)

      }
    }
  }
}
