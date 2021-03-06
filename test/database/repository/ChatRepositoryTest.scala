package database.repository

import api.dtos.{ CreateEmailDTO, CreateShareDTO, CreateUserDTO }
import database.mappings.ChatMappings._
import database.mappings.Destination
import database.mappings.DraftMappings._
import database.mappings.EmailMappings._
import database.mappings.UserMappings._
import database.properties.TestDBProperties
import definedStrings.testStrings.RepositoryStrings._
import generators.Generator
import org.scalatest.{ Matchers, _ }
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

class ChatRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {

  implicit private val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy private val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy private val injector: Injector = appBuilder.injector()
  lazy implicit private val db: Database = TestDBProperties.db

  private val emailActions: EmailRepository = injector.instanceOf[EmailRepository]
  private val chatActions: ChatRepository = injector.instanceOf[ChatRepository]

  private val userGenerator = new Generator()
  private val userCreation = CreateUserDTO(userGenerator.username, userGenerator.password)
  private val userCreationWrongUser = new CreateUserDTO(new Generator().username, userCreation.password)

  private val defaultCreation = new Generator()
  private val emailCreation = new CreateEmailDTO(
    Option(defaultCreation.ID),
    defaultCreation.dateOf,
    defaultCreation.header,
    defaultCreation.body,
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

  /* Verify if a chat is inserted in database */
  ChatRepository + InsertChatFunction should {
    "check if the chat is inserted in the chat table correctly" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        _ <- chatActions.insertChat(emailCreation, resultChatID)
        resultChatTable <- db.run(chatTable.result)
      } yield (resultChatID, resultChatTable)
      result.map {
        case (resultChatID, resultChatTable) =>

          /** Verify if something was inserted in the chat table */
          resultChatTable.nonEmpty shouldBe true

          /** Verify if the chatID in chat Table matches with email inserted */
          resultChatTable.forall(_.chatID === resultChatID) shouldBe true

          /** Verify if the header in chat Table matches with email inserted */
          resultChatTable.forall(_.header === emailCreation.header) shouldBe true
      }
    }
  }

  /* Verify if a chat is inserted in database */
  ChatRepository + GetInboxFunction should {
    "check if the Inbox has the right messages for an specific user" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        resultInbox <- chatActions.getInbox(userCreation.username, isTrash = false)
      } yield (resultChatID, resultInbox)
      result.map {
        case (resultChatID, resultInbox) =>

          /** Verify if Inbox is not empty */
          resultInbox.nonEmpty shouldBe true

          /** Verify if the chatID in chat Table matches with email inserted */
          resultInbox.forall(_.Id === resultChatID) shouldBe true

          /** Verify if the header in chat Table matches with email inserted */
          resultInbox.forall(_.header === emailCreation.header) shouldBe true
      }
    }
  }

  /* Verify if a chat is not inserted in database */
  ChatRepository + GetInboxFunction should {
    "check if the Inbox is empty for an user without messages" in {
      val result = for {
        _ <- emailActions.insertEmail(userCreation.username, emailCreation)
        resultInbox <- chatActions.getInbox(userCreationWrongUser.username, isTrash = false)
      } yield resultInbox
      /** Verify if Inbox is empty */
      result.map(_.isEmpty shouldBe true)

    }
  }

  /* Verify if a the function getEmails selects the right emails through userName and chatID */
  ChatRepository + GetEmailsFunction should {
    "check if the email is returned properly when chatID and username are provided" in {

      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        resultGet <- chatActions.getEmails(userCreation.username, resultChatID, isTrash = false)
        resultEmailTable <- db.run(emailTable.result)
      } yield (resultGet, resultEmailTable)
      /** Verify if Inbox is not empty */
      result.map {
        case (resultGet, resultEmailTable) =>
          resultGet.nonEmpty shouldBe true
          /** Verify if the emailID in chat Table matches with email inserted */
          resultGet.map(_.Id).toSet shouldEqual resultEmailTable.map(_.emailID).toSet
          /** Verify if the header in chat Table matches with email inserted */
          resultGet.forall(_.header === emailCreation.header) shouldBe true
      }
    }
  }

  /* Verify if a the function getEmails selects no emails through wrong userName and chatID */
  ChatRepository + GetEmailsFunction should {
    "check if the email is not returned when chatID and wrong username are provided" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        resultGet <- chatActions.getEmails(userCreationWrongUser.username, resultChatID, isTrash = false)
      } yield resultGet
      /** Verify if Inbox is empty */
      result.map(_.isEmpty shouldBe true)
    }
  }

  /* Verify if a the function getEmails selects no emails through wrong userName and chatID */
  ChatRepository + GetEmailsFunction should {
    "check if the email is returned properly when wrong chatID and username are provided" in {
      emailActions.insertEmail(userCreation.username, emailCreation)
      val resultGet = chatActions.getEmails(userCreation.username, new Generator().ID, isTrash = false)

      /** Verify if Inbox is empty */
      resultGet.map {
        _.isEmpty shouldBe true
      }
    }
  }

  /* Verify if a the function changeTrash changes a mail to trash correctly */
  ChatRepository + ChangeTrashFunction should {
    "check if the changes to trash status are proceeded correctly" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        _ <- chatActions.changeTrash(userCreation.username, resultChatID, moveToTrash = true)
        resultEmailTable <- db.run(emailTable.result)
      } yield resultEmailTable

      result.map(_.forall(_.isTrash === true) shouldBe true)
    }
  }

  /* Verify if a the function changeTrash changes the trash email back to inbox correctly */
  ChatRepository + ChangeTrashFunction should {
    "check if the changes to non trash status are proceeded correctly" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        _ <- chatActions.changeTrash(userCreation.username, resultChatID, moveToTrash = true)
        _ <- chatActions.changeTrash(userCreation.username, resultChatID, moveToTrash = false)
        resultEmailTable <- db.run(emailTable.result)
      } yield resultEmailTable

      result.map(_.forall(_.isTrash === false) shouldBe true)
    }
  }

  /* Verify if a the function changeTrash changes a mail to trash correctly for a recipient*/
  ChatRepository + ChangeTrashFunction should {
    "check if the changes to trash are proceeded correctly for toAddresses" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        _ <- Future.sequence { emailCreation.to.getOrElse(Seq("")).map(chatActions.changeTrash(_, resultChatID, moveToTrash = true)) }
        resultDestinationTable <- db.run(destinationEmailTable.filter(_.destination === Destination.ToAddress).result)
      } yield resultDestinationTable

      result.map(_.forall(_.isTrash === true) shouldBe true)
    }
  }

  /* Verify if a the function changeTrash changes a mail to trash correctly for a recipient*/
  ChatRepository + ChangeTrashFunction should {
    "check if the changes to trash for toAddresses have no effect for CC and BCC" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        _ <- Future.sequence {
          emailCreation.to.getOrElse(Seq(EmptyString)).map(recipient =>
            chatActions.changeTrash(recipient, resultChatID, moveToTrash = true))
        }
        resultDestinationTable <- db.run(destinationEmailTable.filterNot(_.destination === Destination.ToAddress).result)
      } yield resultDestinationTable

      result.map(_.forall(_.isTrash === false) shouldBe true)
    }
  }

  /* Verify if a the function changeTrash changes a mail to trash correctly for a recipient*/
  ChatRepository + ChangeTrashFunction should {
    "check if the changes to trash for toAddresses doesn´t work for wrong username" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        _ <- chatActions.changeTrash(new Generator().emailAddress, resultChatID, moveToTrash = true)
        resultEmailTable <- db.run(emailTable.result)
      } yield resultEmailTable

      result.map(_.forall(_.isTrash === false) shouldBe true)
    }
  }

  /* Verify if a the function changeTrash changes a mail to trash correctly for a recipient*/
  ChatRepository + ChangeTrashFunction should {
    "check if the changes to trash for toAddresses doesn´t work for wrong chatID" in {
      val result = for {
        _ <- emailActions.insertEmail(userCreation.username, emailCreation)
        _ <- chatActions.changeTrash(userCreation.username, new Generator().ID, moveToTrash = true)
        resultEmailTable <- db.run(emailTable.result)
      } yield resultEmailTable

      result.map(_.forall(_.isTrash === false) shouldBe true)
    }
  }

  /* Verify if a the function changeTrash changes a mail to trash correctly for a recipient*/
  ChatRepository + ChangeTrashFunction should {
    "check if the changes to trash for toAddresses doesn´t work for wrong moveToTrash boolean" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        _ <- chatActions.changeTrash(userCreation.username, resultChatID, moveToTrash = false)
        resultEmailTable <- db.run(emailTable.result)
      } yield resultEmailTable

      result.map(_.forall(_.isTrash === false) shouldBe true)
    }
  }

  /* Verify if a the function insertPermission inserts the permission correctly */
  ChatRepository + InsertPermissionFunction should {
    "check if the permission to some chat is proceeded correctly" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        shareCreation = CreateShareDTO(resultChatID, new Generator().emailAddress)
        resultShareID <- chatActions.insertPermission(userCreation.username, shareCreation)
        resultShareTable <- db.run(shareTable.result)
      } yield (resultChatID, shareCreation, resultShareID, resultShareTable)

      /** Verify if resultShareTable is not empty */
      result.map {
        case (resultChatID, shareCreation, resultShareID, resultShareTable) =>

          /** Verify if resultGet is empty */
          resultShareTable.nonEmpty shouldBe true

          /** verify if the parameters of the shareTable are correct */
          resultShareTable.forall(_.chatID === resultChatID) shouldBe true
          resultShareTable.forall(_.fromUser === userCreation.username) shouldBe true
          resultShareTable.forall(_.shareID === resultShareID) shouldBe true
          resultShareTable.forall(_.toUser === shareCreation.supervisor) shouldBe true

      }
    }
  }

  /* Verify if getShares returns the permissions correctly */
  ChatRepository + GetSharesFunction should {
    "check if the emails that were allowed to supervise are returned correctly" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        shareCreation = CreateShareDTO(resultChatID, new Generator().emailAddress)
        _ <- chatActions.insertPermission(userCreation.username, shareCreation)
        returnShares <- chatActions.getShares(shareCreation.supervisor)
      } yield (resultChatID, returnShares)

      result.map {
        case (_, returnShares) =>

          /** Verify if returnShares is not empty */
          returnShares.nonEmpty shouldBe true
          //returnShares.forall(_.Id === resultChatID) shouldBe true
          returnShares.forall(_.header === emailCreation.header) shouldBe true
      }
    }
  }

  /* Verify if getShares doesn't return the permissions to other user */
  ChatRepository + GetSharesFunction should {
    "check if the emails that were allowed to supervise are not returned for other user" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        shareCreation = CreateShareDTO(resultChatID, new Generator().emailAddress)
        _ <- chatActions.insertPermission(userCreation.username, shareCreation)
        returnShares <- chatActions.getShares(new Generator().emailAddress)
      } yield returnShares

      result.map { returnShares =>

        /** Verify if returnShares is not empty */
        returnShares.isEmpty shouldBe true
      }
    }
  }

  /* Verify if getSharedEmails returns the permission emails correctly */
  ChatRepository + GetSharedEmailFunction should {
    "check if the emails that were allowed to supervise are returned correctly" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        resultEmailTable <- db.run(emailTable.result)
        resultEmailID = resultEmailTable.map(row => row.emailID).head
        shareCreation = CreateShareDTO(resultChatID, new Generator().emailAddress)
        resultShareID <- chatActions.insertPermission(userCreation.username, shareCreation)
        returnShares <- chatActions.getSharedEmails(shareCreation.supervisor, resultShareID)
      } yield (returnShares, resultEmailID)

      result.map {
        case (returnShares, resultEmailID) =>

          /** Verify if returnShares is not empty */
          returnShares.nonEmpty shouldBe true

          /** Verify if the parameters returned are correct */
          returnShares.forall(_.Id === resultEmailID) shouldBe true
          returnShares.forall(_.header === emailCreation.header) shouldBe true
      }
    }
  }

  /* Verify if getSharedEmails doesnt return the permission emails for an unauthorized user */
  ChatRepository + GetSharedEmailFunction should {
    "check if the emails that were allowed to supervise are not returned for other user" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        _ <- db.run(emailTable.result)
        shareCreation = CreateShareDTO(resultChatID, new Generator().emailAddress)
        resultShareID <- chatActions.insertPermission(userCreation.username, shareCreation)
        returnShares <- chatActions.getSharedEmails(new Generator().emailAddress, resultShareID)
      } yield returnShares

      /** Verify if returnShares is empty */
      result.map(_.isEmpty shouldBe true)
    }
  }

  /* Verify if getSharedEmail return a specific email correctly */
  ChatRepository + GetSharedEmailFunction should {
    "check if a specific email that was allowed to supervise is returned correctly" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        resultEmailTable <- db.run(emailTable.result)
        resultEmailID = resultEmailTable.map(row => row.emailID).head
        shareCreation = CreateShareDTO(resultChatID, new Generator().emailAddress)
        resultShareID <- chatActions.insertPermission(userCreation.username, shareCreation)
        returnShare <- emailActions.getSharedEmail(shareCreation.supervisor, resultShareID, resultEmailID)
      } yield (resultChatID, returnShare)

      result.map {
        case (resultChatID, returnShare) =>

          /** Verify if the parameters returned are correct */
          (returnShare.chatID === resultChatID) shouldBe true
          (returnShare.fromAddress === userCreation.username) shouldBe true
          (returnShare.fromAddress === userCreation.username) shouldBe true
          returnShare.username.toSet shouldEqual emailCreation.to.getOrElse(Seq()).toSet
          (returnShare.header === emailCreation.header) shouldBe true
          (returnShare.body === emailCreation.body) shouldBe true
          (returnShare.dateOf === emailCreation.dateOf) shouldBe true
      }
    }
  }

  /* Verify if getSharedEmail doesn´t return a specific email to the wrong user */
  ChatRepository + GetSharedEmailFunction should {
    "check if a specific email that was allowed to supervise is not accessed by the wrong user" in {
      val result = for {
        resultChatID <- emailActions.insertEmail(userCreation.username, emailCreation)
        resultEmailTable <- db.run(emailTable.result)
        resultEmailID = resultEmailTable.map(row => row.emailID).head
        shareCreation = CreateShareDTO(resultChatID, new Generator().emailAddress)
        resultShareID <- chatActions.insertPermission(userCreation.username, shareCreation)
        returnShare <- emailActions.getSharedEmail(new Generator().emailAddress, resultShareID, resultEmailID)
      } yield returnShare

      /** Verify if returnShares is empty */
      result.map(_.body shouldBe EmptyString)
      result.map(_.chatID shouldBe EmptyString)
      result.map(_.dateOf shouldBe EmptyString)
      result.map(_.fromAddress shouldBe EmptyString)
      result.map(_.header shouldBe EmptyString)
      result.map(_.username shouldBe Seq())
    }
  }

  /* Verify if deletePermission takes the permission from the supervised user*/
  ChatRepository + DeletePermissionFunction should {
    "check if a an user is not allowed to access the emails anymore after permission deleted" in {
      val resultChatID = Await.result(emailActions.insertEmail(userCreation.username, emailCreation), Duration.Inf)
      val resultEmailTable = Await.result(db.run(emailTable.result), Duration.Inf)
      resultEmailTable.map(row => row.emailID).head
      val shareCreation = new CreateShareDTO(resultChatID, new Generator().emailAddress)
      val resultShareID = Await.result(chatActions.insertPermission(userCreation.username, shareCreation), Duration.Inf)
      Await.result(chatActions.deletePermission(userCreation.username, shareCreation.supervisor, resultChatID), Duration.Inf)
      val returnShares = Await.result(chatActions.getSharedEmails(shareCreation.supervisor, resultShareID), Duration.Inf)
      /** Verify if returnShares is not empty */
      assert(returnShares.isEmpty)
    }
  }

}

