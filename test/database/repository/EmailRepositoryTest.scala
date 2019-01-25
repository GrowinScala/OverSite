package database.repository

import java.util.UUID

import api.dtos.{ CreateEmailDTO, CreateUserDTO, EmailInfoDTO, MinimalInfoDTO }
import database.mappings.ChatMappings.{ chatTable, shareTable }
import database.mappings.Destination
import database.mappings.DraftMappings.destinationDraftTable
import database.mappings.EmailMappings._
import database.mappings.UserMappings.{ loginTable, userTable }
import database.properties.TestDBProperties
import definedStrings.testStrings.RepositoryStrings._
import generators._
import org.scalatest._
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._
import org.scalatest.Matchers

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.Try

class EmailRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = TestDBProperties.db

  val emailActions = injector.instanceOf[EmailRepository]

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
    Option(new Generator().emailAddresses))

  val DraftCreation = new Generator()
  val emailDraftCreation = new CreateEmailDTO(
    Option(DraftCreation.ID),
    DraftCreation.dateOf,
    DraftCreation.header,
    DraftCreation.body,
    Option(new Generator().emailAddresses),
    Option(new Generator().emailAddresses),
    Option(new Generator().emailAddresses))

  private val tables = Seq(chatTable, userTable, emailTable, destinationEmailTable, destinationDraftTable, loginTable, shareTable)

  override def beforeAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.create): _*)), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.drop): _*)), Duration.Inf)
  }

  override def beforeEach(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.delete): _*)), Duration.Inf)
  }

  /** Verify if an email is inserted in database correctly */
  EmailRepository + " #insertEmail" should {
    "check if the intended email is inserted in the email table in database" in {

      val result = for {
        _ <- emailActions.insertEmail(userCreation.username, emailCreation)
        resultEmailTable <- db.run(emailTable.result)
      } yield resultEmailTable

      result.map { seqEmailRow =>

        /** Verify if the email table is not empty **/
        seqEmailRow.nonEmpty shouldEqual true

        /** Verify if the respective arguments match **/
        seqEmailRow.forall(_.fromAddress === userCreation.username) shouldBe true

        seqEmailRow.forall(_.header === emailCreation.header) shouldBe true

        seqEmailRow.forall(_.body === emailCreation.body) shouldBe true

        seqEmailRow.forall(_.dateOf === emailCreation.dateOf) shouldBe true

        /** Verify if emailID and chatID have an UUID format **/

        Try[Boolean] {
          UUID.fromString(seqEmailRow.head.chatID)
          true
        }.getOrElse(false) shouldEqual true

        Try[Boolean] {
          UUID.fromString(seqEmailRow.head.emailID)
          true
        }.getOrElse(false) shouldEqual true

      }
    }
  }

  /** Verify if a second email is inserted in database correctly with the right chatID */
  EmailRepository + " #insertEmail" should {
    "check if a second email with the same chatID as first is inserted in the email table in database" in {

      val result = for {
        resultChatIDFirst <- emailActions.insertEmail(userCreation.username, emailCreation)
        /* Insertion of a second email with the same chatID as the first one*/
        resultChatIDSecond <- emailActions.insertEmail(userCreation.username, new CreateEmailDTO(
          Option(resultChatIDFirst),
          defaultCreation.dateOf,
          defaultCreation.header,
          defaultCreation.body,
          Option(new Generator().emailAddresses),
          Option(new Generator().emailAddresses),
          Option(new Generator().emailAddresses)))
      } yield (resultChatIDFirst, resultChatIDSecond)

      result.map {
        case (resultChatIDFirst, resultChatIDSecond) =>
          resultChatIDFirst shouldEqual resultChatIDSecond
      }
    }

    /** Verify if an email is inserted in chatTable correctly */
    EmailRepository + " #insertEmail" should {
      "check if the chat parameters are inserted in the chat table in database" in {

        val result = for {

          _ <- emailActions.insertEmail(userCreation.username, emailCreation)
          resultChatTable <- db.run(chatTable.result)

        } yield resultChatTable

        /** Verify if the chat table is not empty **/
        result.map(_.nonEmpty shouldEqual true)

        /** Verify if the respective arguments match **/

        result.map(_.head.header shouldEqual userCreation.username)

        /** Verify if chatID have an UUID format **/

        Try[Boolean] {
          result.map(seqChatRow => UUID.fromString(seqChatRow.head.chatID))
          true
        }
          .getOrElse(false) shouldEqual true
      }
    }

    /** Verify if an email is inserted in the toAddress table correctly */
    EmailRepository + " #insertEmail" should {
      "check if the to is inserted in the toAddress table in database" in {

        val result = for {
          _ <- emailActions.insertEmail(userCreation.username, emailCreation)
          resultToAddressTable <- db.run(destinationEmailTable.filter(_.destination === Destination.ToAddress).result)
          resultEmailTable <- db.run(emailTable.result)
        } yield (resultToAddressTable, resultEmailTable)

        emailCreation.to match {
          case Some(to) if to.nonEmpty =>

            result.map {
              case (tosTable, emailsTable) =>

                /** If the parameter TO exists it is verified if the toAddress table is not empty */
                tosTable.nonEmpty shouldEqual true

                /** Verify if the username of toAddress table is the same as toAddress parameter of email inserted */
                tosTable.map(_.username).toSet shouldEqual emailCreation.to.get.toSet

                /** Verify if the sequence of toID have an UUID format **/
                Try[Boolean] {
                  tosTable.map(
                    toRow => UUID.fromString(toRow.emailID))
                  true
                }.getOrElse(false) shouldEqual true

                /** Verify if the sequence of emailID have an UUID format **/

                Try[Boolean] {
                  tosTable.map(
                    toRow => UUID.fromString(toRow.emailID))
                  true
                }.getOrElse(false) shouldEqual true

                /** Verify if emailID of email table is the same as toAddress table */
                emailsTable.forall(emailRow =>
                  tosTable.map(toRow => toRow.emailID).contains(emailRow.emailID)) shouldEqual true

            }

          case _ =>

            result.map {
              case (tosTable, _) =>

                /** If the parameter TO does not exists it is verified if the CC table is empty */
                tosTable.isEmpty shouldEqual true
            }
        }
      }
    }

    /** Verify if an email is inserted in BCC table correctly */
    EmailRepository + " #insertEmail" should {
      "check if the BCC is inserted in the BCC table in database" in {

        val result = for {

          _ <- emailActions.insertEmail(userCreation.username, emailCreation)

          resultBCCtable <- db.run(destinationEmailTable.filter(_.destination === Destination.BCC).result)
          resultEmailTable <- db.run(emailTable.result)
        } yield (resultBCCtable, resultEmailTable)

        /** Verify if the bcc table is created only if necessary **/
        emailCreation.BCC match {
          case Some(_) =>

            result.map {
              case (bccsTable, emailsTable) =>

                /** If the parameter BCC exists it is verified if the BCC table is not empty */
                bccsTable.nonEmpty shouldEqual true

                /** Verify if the username of BCC table is the same as BCC parameter of email inserted */
                bccsTable.map(_.username).toSet shouldEqual emailCreation.BCC.get.toSet

                /** Verify if sequence of bccIDs have an UUID format **/
                Try[Boolean] {
                  bccsTable.map(row => UUID.fromString(row.emailID))
                  true
                }.getOrElse(false) shouldEqual true

                /** Verify if the sequence of emailID have an UUID format **/
                Try[Boolean] {
                  bccsTable.map(row => UUID.fromString(row.emailID))
                  true
                }.getOrElse(false) shouldEqual true

                /** Verify if emailID of email table is the same as BCC table */
                emailsTable.forall(emailRow =>
                  bccsTable.map(bccRow => bccRow.emailID).contains(emailRow.emailID)) shouldEqual true
            }

          case _ =>

            result.map {
              case (bccsTable, _) =>

                /** If the parameter BCC does not exists it is verified if the BCC table is empty */
                bccsTable.isEmpty shouldEqual true
            }
        }
      }
    }

    /** Verify if an email is inserted in CC table correctly */
    EmailRepository + " #insertEmail" should {
      "check if the CC parameters are inserted in the CC table in database when necessary" in {

        val result = for {

          _ <- emailActions.insertEmail(userCreation.username, emailCreation)
          resultCCtable <- db.run(destinationEmailTable.filter(_.destination === Destination.CC).result)
          resultEmailTable <- db.run(emailTable.result)

        } yield (resultCCtable, resultEmailTable)

        /** Verify if the cc table is created only if necessary **/
        emailCreation.BCC match {
          case Some(_) =>

            result.map {
              case (ccsTable, emailsTable) =>

                /** If the parameter CC exists it is verified if the CC table is not empty */
                ccsTable.nonEmpty shouldEqual true

                /** Verify if the username of CC table is the same as CC parameter of email inserted */
                ccsTable.map(_.username).toSet shouldEqual emailCreation.CC.get.toSet

                /** Verify if sequence of ccIDs have an UUID format **/
                Try[Boolean] {
                  ccsTable.map(row => UUID.fromString(row.emailID))
                  true
                }.getOrElse(false) shouldEqual true

                /** Verify if the sequence of emailID have an UUID format **/
                Try[Boolean] {
                  ccsTable.map(row => UUID.fromString(row.emailID))
                  true
                }.getOrElse(false) shouldEqual true

                /** Verify if emailID of email table is the same as CC table */
                emailsTable.forall(emailRow =>
                  ccsTable.map(ccRow => ccRow.emailID).contains(emailRow.emailID)) shouldBe true
            }

          case _ =>

            result.map {
              case (ccsTable, _) =>

                /** If the parameter CC does not exists it is verified if the CC table is empty */
                ccsTable.isEmpty shouldEqual true
            }
        }
      }
    }

    /** Verify the function changeTrash */
    EmailRepository + "#changeTrash" should {
      "check if the function changeTrash is able to change the trash status of the inserted email" in {
        val result = for {
          _ <- emailActions.insertEmail(userCreation.username, emailCreation)
          resultEmailTable <- db.run(emailTable.result)
          _ <- Future.sequence { resultEmailTable.map(emailRow => emailActions.changeTrash(userCreation.username, emailRow.emailID, moveToTrash = true)) }
          resultEmailTableTrash <- db.run(emailTable.result)
        } yield (resultEmailTable, resultEmailTableTrash)

        result.map {
          case (resultEmailTable, resultEmailTableTrash) =>

            /** Verify if the emails are not in trash when inserted */
            resultEmailTable.forall(_.isTrash === false) shouldBe true

            /** Verify if the emails are in trash after changeTrash */
            resultEmailTableTrash.forall(_.isTrash === true) shouldBe true
        }
      }
    }

    /** Verify the function getEmails */
    EmailRepository + "#getEmails" should {
      "check if the function getEmails is able to reach the inserted email" in {

        val result = for {
          _ <- emailActions.insertEmail(userCreation.username, emailCreation)
          resultEmailTable <- db.run(emailTable.result)
          resultSent <- emailActions.getEmails(userCreation.username, "sent")
          resultReceived <- Future.sequence(emailCreation.to.get.map(emailActions.getEmails(_, "received")))
        } yield (resultEmailTable, resultSent, resultReceived)

        /** getEmails for sent and received cases */
        result.map {
          case (resultEmailTable, resultSent, resultReceived) =>

            resultEmailTable.map(emailsRow => MinimalInfoDTO(emailsRow.emailID, emailsRow.header)) shouldEqual resultSent

            resultReceived.forall(seqEmailInfo =>
              seqEmailInfo === resultEmailTable.map(row =>
                MinimalInfoDTO(row.emailID, row.header))) shouldBe true

        }
      }
    }

    /** Verify the function getEmail **/
    EmailRepository + " #getOneEmail" should {
      "check if the function getEmail is able to reach the email inserted" in {

        val result = for {
          _ <- emailActions.insertEmail(userCreation.username, emailCreation)
          resultEmailTable <- db.run(emailTable.result)
          resultSent <- Future.sequence{resultEmailTable.map(x=>emailActions.getEmail(userCreation.username, "sent", x.emailID))}
          resultReceived <- Future.sequence(emailCreation.to.get.map(to =>
            emailActions.getEmail(to, "received", resultEmailTable.map(emailRow =>
              emailRow.emailID).head)).map(seqEmailInfoDto => seqEmailInfoDto))
          resultTos <- Future.successful(EmailInfoDTO(
            resultEmailTable.head.chatID,
            resultEmailTable.head.fromAddress,
            emailCreation.to.getOrElse(Seq("")),
            resultEmailTable.head.header,
            resultEmailTable.head.body,
            resultEmailTable.head.dateOf))
        } yield (resultSent, resultReceived, resultTos)

        result.map {
          case (resultSent, resultReceived, resultTos) =>
            resultSent shouldEqual resultTos
            resultReceived.map(_.contains(resultTos)) shouldBe true

        }
      }
    }
  }
}

