package database.repository

import java.util.UUID

import api.dtos._
import database.mappings.ChatMappings.{ chatTable, shareTable }
import database.mappings.Destination
import database.mappings.DraftMappings.{ destinationDraftTable, draftTable }
import database.mappings.EmailMappings._
import database.mappings.UserMappings.{ loginTable, userTable }
import database.properties.TestDBProperties
import definedStrings.testStrings.RepositoryStrings._
import definedStrings.DatabaseStrings._
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

  val emailActions: EmailRepository = injector.instanceOf[EmailRepository]

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

  private val tables = Seq(chatTable, userTable, emailTable, destinationEmailTable, draftTable, destinationDraftTable, loginTable, shareTable)

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

    /** Verify the function getEmails */
    EmailRepository + "#getEmails" should {
      "check if the function getEmails is able to reach the inserted email that is trashed" in {

        val result = for {
          _ <- emailActions.insertEmail(userCreation.username, emailCreation)
          resultEmailTable <- db.run(emailTable.result)
          _ <- Future.sequence { resultEmailTable.map(x => emailActions.changeTrash(userCreation.username, x.emailID, moveToTrash = true)) }
          resultToTrash <- emailActions.getEmails(userCreation.username, "trashed")
        } yield (resultEmailTable, resultToTrash)

        /** getEmails for sent and received cases */
        result.map {
          case (resultEmailTable, resultToTrash) =>
            resultEmailTable.map(emailsRow => MinimalInfoDTO(emailsRow.emailID, emailsRow.header)) shouldEqual resultToTrash

        }
      }
    }

    /** Verify the function getEmail **/
    EmailRepository + " #getEmail" should {
      "check if the function getEmail is able to reach the email inserted" in {

        val result = for {
          _ <- emailActions.insertEmail(userCreation.username, emailCreation)
          resultEmailTable <- db.run(emailTable.result)
          resultSent <- Future.sequence { resultEmailTable.map(x => emailActions.getEmail(userCreation.username, "sent", x.emailID)) }
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
        } yield (resultSent.flatten, resultReceived.flatten, resultTos)

        result.map {
          case (resultSent, resultReceived, resultTos) =>
            resultSent.forall(emailInfoDTO => emailInfoDTO === resultTos) shouldBe true
            resultReceived.forall(emailInfoDTO => emailInfoDTO === resultTos) shouldBe true
        }
      }
    }
  }

  /** Verify the function getEmail **/
  EmailRepository + " #getEmail" should {
    "check if the function getEmail is able to reach the email inserted and trashed" in {

      val result = for {
        _ <- emailActions.insertEmail(userCreation.username, emailCreation)
        resultEmailTable <- db.run(emailTable.result)
        resultSent <- Future.sequence { resultEmailTable.map(x => emailActions.getEmail(userCreation.username, "sent", x.emailID)) }
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
      } yield (resultSent.flatten, resultReceived.flatten, resultTos)

      result.map {
        case (resultSent, resultReceived, resultTos) =>
          resultSent.forall(emailInfoDTO => emailInfoDTO === resultTos) shouldBe true
          resultReceived.forall(emailInfoDTO => emailInfoDTO === resultTos) shouldBe true

      }
    }
  }

  /**DRAFT REPOSITORIES TESTS*/
  /** Verify if an email is inserted in database correctly */
  EmailRepository + " #insertDraft" should {
    "check if the intended draft is inserted in the draft table in database" in {

      val result = for {
        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)
        resultDraftTable <- db.run(draftTable.result)
      } yield resultDraftTable

      result.map { seqDraftRow =>

        /** Verify if the email table is not empty **/
        seqDraftRow.nonEmpty shouldEqual true

        /** Verify if the respective arguments match **/
        seqDraftRow.forall(_.fromAddress === userCreation.username) shouldBe true

        seqDraftRow.forall(_.header === emailDraftCreation.header) shouldBe true

        seqDraftRow.forall(_.body === emailDraftCreation.body) shouldBe true

        seqDraftRow.forall(_.dateOf === emailDraftCreation.dateOf) shouldBe true

        /** Verify if emailID and chatID have an UUID format **/

        Try[Boolean] {
          UUID.fromString(seqDraftRow.head.chatID)
          true
        }.getOrElse(false) shouldEqual true

        Try[Boolean] {
          UUID.fromString(seqDraftRow.head.draftID)
          true
        }.getOrElse(false) shouldEqual true

      }
    }
  }

  /** Verify if a second email is inserted in database correctly with the right chatID */
  EmailRepository + " #insertDraft" should {
    "check if a second draft with the same chatID as first is inserted in the draft table in database" in {

      val result = for {
        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)
        resultChatIDFirst <- db.run(draftTable.map(_.chatID).result)
        /* Insertion of a second email with the same chatID as the first one*/
        _ <- Future.sequence(resultChatIDFirst.map(chatID => emailActions.insertDraft(
          userCreation.username,
          new CreateEmailDTO(
            Option(chatID),
            defaultCreation.dateOf,
            defaultCreation.header,
            defaultCreation.body,
            Option(new Generator().emailAddresses),
            Option(new Generator().emailAddresses),
            Option(new Generator().emailAddresses)))))
        resultChatIDSecond <- db.run(draftTable.map(_.chatID).result)

      } yield (resultChatIDFirst, resultChatIDSecond)

      result.map {
        case (resultChatIDFirst, resultChatIDSecond) =>
          resultChatIDFirst.toSet shouldEqual resultChatIDSecond.toSet
      }
    }
  }

  /** Verify if a draft is inserted without destinations */
  EmailRepository + " #insertDraft" should {
    "check if a draft is properly inserted with no destination" in {

      val result = for {
        /* Insertion od draft without destinations*/
        _ <- emailActions.insertDraft(
          userCreation.username,
          new CreateEmailDTO(
            Option(defaultCreation.ID),
            defaultCreation.dateOf,
            defaultCreation.header,
            defaultCreation.body,
            None,
            None,
            None))

        resultDraftTable <- db.run(draftTable.result)
        resultDestinationTable <- db.run(destinationDraftTable.result)

      } yield (resultDraftTable, resultDestinationTable)

      result.map {
        case (resultDraftTable, resultDestinationTable) =>
          resultDraftTable.nonEmpty shouldBe true
          resultDestinationTable.isEmpty shouldBe true
      }
    }
  }

  /** Verify if an email is inserted in the toAddress table correctly */
  EmailRepository + " #insertDraft" should {
    "check if the to is inserted in the destination draft table in database" in {

      val result = for {
        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)
        resultDestinationTable <- db.run(destinationDraftTable.filter(_.destination === Destination.ToAddress).result)
        resultDraftTable <- db.run(draftTable.result)
      } yield (resultDestinationTable, resultDraftTable)

      emailDraftCreation.to match {
        case Some(_) =>
          result.map {
            case (toTable, resultDraftTable) =>

              /** If the parameter TO exists it is verified if the destination table is not empty */
              toTable.nonEmpty shouldEqual true

              /** Verify if the username of toAddress table is the same as toAddress parameter of draft inserted */
              toTable.map(_.username).toSet shouldEqual emailDraftCreation.to.get.toSet

              /** Verify if the sequence of toID have an UUID format **/
              Try[Boolean] {
                toTable.map(
                  toRow => UUID.fromString(toRow.draftID))
                true
              }.getOrElse(false) shouldEqual true

              /** Verify if the sequence of draftID have an UUID format **/

              Try[Boolean] {
                toTable.map(
                  toRow => UUID.fromString(toRow.draftID))
                true
              }.getOrElse(false) shouldEqual true

              /** Verify if draftID of draft table is the same as draftDestination table */
              resultDraftTable.forall(draftRow =>
                toTable.map(toRow => toRow.draftID).contains(draftRow.draftID)) shouldEqual true
          }
        case _ =>

          result.map {
            case (toTable, _) =>

              /** If the parameter To does not exists it is verified if the draftDestination table is empty */
              toTable.isEmpty shouldEqual true
          }
      }
    }
  }

  /** Verify if an draft is inserted with correct BCC */
  EmailRepository + " #insertDraft" should {
    "check if the BCC is inserted in the destination table in database" in {

      val result = for {

        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)

        resultBCCTable <- db.run(destinationDraftTable.filter(_.destination === Destination.BCC).result)
        resultDraftTable <- db.run(draftTable.result)
      } yield (resultBCCTable, resultDraftTable)

      /** Verify if the bcc table is created only if necessary **/
      emailDraftCreation.BCC match {
        case Some(_) =>

          result.map {
            case (bccTable, resultDraftTable) =>

              /** If the parameter BCC exists it is verified if the BCC table is not empty */
              bccTable.nonEmpty shouldEqual true

              /** Verify if the username of BCC table is the same as BCC parameter of email inserted */
              bccTable.map(_.username).toSet shouldEqual emailDraftCreation.BCC.get.toSet

              /** Verify if sequence of bccIDs have an UUID format **/
              Try[Boolean] {
                bccTable.map(row => UUID.fromString(row.draftID))
                true
              }.getOrElse(false) shouldEqual true

              /** Verify if the sequence of emailID have an UUID format **/
              Try[Boolean] {
                bccTable.map(row => UUID.fromString(row.draftID))
                true
              }.getOrElse(false) shouldEqual true

              /** Verify if draftID of draft table is the same as BCC table */
              resultDraftTable.forall(emailRow =>
                bccTable.map(bccRow => bccRow.draftID).contains(emailRow.draftID)) shouldEqual true
          }

        case _ =>

          result.map {
            case (bccTable, _) =>

              /** If the parameter BCC does not exists it is verified if the BCC table is empty */
              bccTable.isEmpty shouldEqual true
          }
      }
    }
  }

  /** Verify if an draft is inserted in CC table correctly */
  EmailRepository + " #insertDraft" should {
    "check if the CC parameters are inserted in the draft destination table in database when necessary" in {

      val result = for {

        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)
        resultCCTable <- db.run(destinationDraftTable.filter(_.destination === Destination.CC).result)
        resultDraftTable <- db.run(draftTable.result)

      } yield (resultCCTable, resultDraftTable)

      /** Verify if the cc table is created only if necessary **/
      emailDraftCreation.CC match {
        case Some(_) =>

          result.map {
            case (ccTable, resultDraftTable) =>

              /** If the parameter CC exists it is verified if the CC table is not empty */
              ccTable.nonEmpty shouldEqual true

              /** Verify if the username of CC table is the same as CC parameter of email inserted */
              ccTable.map(_.username).toSet shouldEqual emailDraftCreation.CC.get.toSet

              /** Verify if sequence of ccIDs have an UUID format **/
              Try[Boolean] {
                ccTable.map(row => UUID.fromString(row.draftID))
                true
              }.getOrElse(false) shouldEqual true

              /** Verify if the sequence of emailID have an UUID format **/
              Try[Boolean] {
                ccTable.map(row => UUID.fromString(row.draftID))
                true
              }.getOrElse(false) shouldEqual true

              /** Verify if emailID of email table is the same as CC table */
              resultDraftTable.forall(emailRow =>
                ccTable.map(ccRow => ccRow.draftID).contains(emailRow.draftID)) shouldBe true
          }

        case _ =>

          result.map {
            case (ccTable, _) =>

              /** If the parameter CC does not exists it is verified if the destinationDraft Table  is empty */
              ccTable.isEmpty shouldEqual true
          }
      }
    }
  }

  /** Verify the function moveInOutTrash */
  EmailRepository + "#moveInOutTrash" should {
    "check if the function moveInOutTrash is able to change the trash status of the inserted draft" in {
      val result = for {
        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)
        resultDraftTable <- db.run(draftTable.result)
        _ <- Future.sequence { resultDraftTable.map(draftRow => emailActions.moveInOutTrash(userCreation.username, draftRow.draftID, trash = true)) }
        resultDraftTableTrash <- db.run(draftTable.result)
      } yield (resultDraftTable, resultDraftTableTrash)

      result.map {
        case (resultDraftTable, resultDraftTableTrash) =>

          /** Verify if the emails are not in trash when inserted */
          resultDraftTable.forall(_.isTrash === false) shouldBe true

          /** Verify if the emails are in trash after changeTrash */
          resultDraftTableTrash.forall(_.isTrash === true) shouldBe true
      }
    }
  }
  /**
   * WARNING: updateDraft is not possible to test since the following Slick exception:
   * org.h2.jdbc.JdbcSQLException: Syntax error in SQL statement "delete ""DRAFTSDESTINATION"" from[*] ""DRAFTSDESTINATION"" where ""DRAFTSDESTINATION"".""DRAF
   * TID"" = 'eddea02a-cdab-47d1-88aa-95984fc007c0' "; SQL statement:
   */

  /** Verify if an update is proceeded in draftTable correctly */
  EmailRepository + " #getDrafts" should {
    "check if the mails are reached in draft table in database" in {

      val result = for {
        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)
        resultDraftTable <- db.run(draftTable.result)
        resultNoTrash <- emailActions.getDrafts(userCreation.username, isTrash = false)
        resultTrash <- emailActions.getDrafts(userCreation.username, isTrash = true)
      } yield (resultDraftTable, resultNoTrash, resultTrash)

      /** getEmails for trash and noTrash cases */
      result.map {
        case (resultDraftTable, resultNoTrash, resultTrash) =>
          resultDraftTable.map(draftRow => MinimalInfoDTO(draftRow.draftID, draftRow.header)) shouldEqual resultNoTrash
          resultTrash.isEmpty shouldBe true
      }
    }
  }

  /** Verify the function getDrafts */
  EmailRepository + "#getDrafts" should {
    "check if the function getDrafts is able to reach the inserted draft that is trashed" in {

      val result = for {
        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)
        resultAuxDraftTable <- db.run(draftTable.result)
        _ <- Future.sequence { resultAuxDraftTable.map(draftRow => emailActions.moveInOutTrash(userCreation.username, draftRow.draftID, trash = true)) }
        resultDraftTable <- db.run(draftTable.result)
        resultTrash <- emailActions.getDrafts(userCreation.username, isTrash = true)
        resultNoTrash <- emailActions.getDrafts(userCreation.username, isTrash = false)
      } yield (resultDraftTable, resultNoTrash, resultTrash)

      /** getEmails for trash and noTrash cases */
      result.map {
        case (resultDraftTable, resultNoTrash, resultTrash) =>
          resultDraftTable.map(draftRow => MinimalInfoDTO(draftRow.draftID, draftRow.header)) shouldEqual resultTrash
          resultNoTrash.isEmpty shouldBe true
      }
    }
  }

  /** Verify the function getDraft **/
  EmailRepository + " #getDraft" should {
    "check if the function getDraft is able to reach the draft inserted" in {

      val result = for {
        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)
        resultDraftTable <- db.run(draftTable.result)
        resultGetTrash <- Future.sequence { resultDraftTable.map(draftRow => emailActions.getDraft(userCreation.username, draftRow.draftID, isTrash = true)) }
        resultGetNoTrash <- Future.sequence { resultDraftTable.map(draftRow => emailActions.getDraft(userCreation.username, draftRow.draftID, isTrash = false)) }
        resultTos <- Future.successful(DraftInfoDTO(
          resultDraftTable.head.draftID,
          userCreation.username,
          emailDraftCreation.to.getOrElse(Seq("")),
          emailDraftCreation.CC.getOrElse(Seq("")),
          emailDraftCreation.BCC.getOrElse(Seq("")),
          resultDraftTable.head.header,
          resultDraftTable.head.body,
          resultDraftTable.head.dateOf))

      } yield (resultGetTrash.flatten, resultGetNoTrash.flatten, resultTos)

      result.map {
        case (resultGetTrash, resultGetNoTrash, resultTos) =>
          resultGetNoTrash.forall(emailInfoDTO => emailInfoDTO === resultTos) shouldBe true
          resultGetTrash.isEmpty shouldBe true
      }
    }
  }

  /** Verify the function getDraft **/
  EmailRepository + " #getDraft" should {
    "check if the function getDraft is able to reach the draft inserted and trashed" in {

      val result = for {
        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)
        resultDraftTable <- db.run(draftTable.result)
        _ <- Future.sequence { resultDraftTable.map(draftRow => emailActions.moveInOutTrash(userCreation.username, draftRow.draftID, trash = true)) }
        resultGetTrash <- Future.sequence { resultDraftTable.map(draftRow => emailActions.getDraft(userCreation.username, draftRow.draftID, isTrash = true)) }
        resultGetNoTrash <- Future.sequence { resultDraftTable.map(draftRow => emailActions.getDraft(userCreation.username, draftRow.draftID, isTrash = false)) }
        resultTos <- Future.successful(DraftInfoDTO(
          resultDraftTable.head.draftID,
          userCreation.username,
          emailDraftCreation.to.getOrElse(Seq("")),
          emailDraftCreation.CC.getOrElse(Seq("")),
          emailDraftCreation.BCC.getOrElse(Seq("")),
          resultDraftTable.head.header,
          resultDraftTable.head.body,
          resultDraftTable.head.dateOf))
      } yield (resultGetTrash.flatten, resultGetNoTrash.flatten, resultTos)

      result.map {
        case (resultGetTrash, resultGetNoTrash, resultTos) =>
          resultGetTrash.forall(emailInfoDTO => emailInfoDTO === resultTos) shouldBe true
          resultGetNoTrash.isEmpty shouldBe true
      }
    }
  }

  /** Verify the function destinations **/
  EmailRepository + "#destinations" should {
    "check if the function destinations is able to reach the destinations inserted in draft" in {

      val result = for {
        _ <- emailActions.insertDraft(userCreation.username, emailDraftCreation)
        resultAuxDraftTable <- db.run(draftTable.result)
        resultDestination <- Future.sequence { resultAuxDraftTable.map(draftRow => emailActions.destinations(userCreation.username, draftRow.draftID)) }
      } yield resultDestination

      result.map { resultDestination =>
        resultDestination.forall(triplet => emailDraftCreation.to.getOrElse(Seq("")).toSet === triplet._1.toSet) shouldBe true
        resultDestination.forall(triplet => emailDraftCreation.BCC.getOrElse(Seq("")).toSet === triplet._2.toSet) shouldBe true
        resultDestination.forall(triplet => emailDraftCreation.CC.getOrElse(Seq("")).toSet === triplet._3.toSet) shouldBe true
      }
    }
  }

  /** Verify the function destinations **/
  EmailRepository + "#destinations" should {
    "check if the function destinations is able return empty for no destinations inserted" in {

      val result = for {
        resultAuxDraftTable <- db.run(draftTable.result)
        resultDestination <- Future.sequence { resultAuxDraftTable.map(draftRow => emailActions.destinations(userCreation.username, draftRow.draftID)) }
      } yield resultDestination

      result.map { resultDestination =>
        resultDestination.forall(triplet => emailDraftCreation.to.getOrElse(Seq("")).isEmpty) shouldBe true
        resultDestination.forall(triplet => emailDraftCreation.BCC.getOrElse(Seq("")).isEmpty) shouldBe true
        resultDestination.forall(triplet => emailDraftCreation.CC.getOrElse(Seq("")).isEmpty) shouldBe true
      }
    }
  }

  /** Verify the function hasdestinations **/
  EmailRepository + "#hasDestination" should {
    "check if the function hasDestination is able return true or false properly" in {

      val result = for {
        falseResult <- emailActions.hasDestination(Seq(), Seq(), Seq())
        trueResult <- emailActions.hasDestination(emailDraftCreation.to.getOrElse(Seq("")), emailDraftCreation.BCC.getOrElse(Seq("")), emailDraftCreation.CC.getOrElse(Seq("")))
      } yield (falseResult, trueResult)

      result.map {
        case (falseResult, trueResult) =>
          falseResult shouldBe false
          trueResult shouldBe true
      }
    }
  }

}

