
package database.repository

import java.util.UUID.randomUUID

import api.dtos.{ CreateEmailDTO, DraftInfoDTO, EmailInfoDTO, MinimalInfoDTO }
import database.mappings.ChatMappings.chatTable
import database.mappings.DraftMappings.{ destinationDraftTable, draftTable }
import database.mappings.EmailMappings.{ emailTable, _ }
import database.mappings._
import database.properties.DBProperties
import definedStrings.ApiStrings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

/**  Class that receives a db path */
class EmailRepositoryImpl @Inject() (dbClass: DBProperties)(implicit val executionContext: ExecutionContext) extends EmailRepository {

  val db = dbClass.db

  /**
   * Insert a chat into database
   * @param email email passed on json body
   * @param chatID chatID
   * @return
   */
  private def insertChatActions(email: CreateEmailDTO, chatID: String): Future[String] = {

    val randomChatID = randomUUID().toString

    existChatID(chatID).flatMap {
      case true => Future { chatID }
      case false =>
        for {
          _ <- db.run(chatTable += ChatRow(randomChatID, email.header))
        } yield randomChatID
    }

  }

  /**
   * Aims to find an chatID already exists in the database
   * @param chatID Reference to an email conversation
   * @return True or False depending if the chatID exists or not
   */
  private def existChatID(chatID: String): Future[Boolean] = {

    val tableSearch = chatTable
      .filter(_.chatID === chatID)
      .result

    db.run(tableSearch).map(_.length).map {
      case 1 => true
      case _ => false
    }
  }

  /**
   * Inserts an email in the database
   * @return Generated chat ID
   */
  def insertEmail(username: String, email: CreateEmailDTO): Future[String] = {

    val randomEmailID = randomUUID().toString

    val chatId = insertChatActions(email, email.chatID.getOrElse(randomUUID().toString))

    chatId.flatMap(id => {
      val insertEmail = for {
        _ <- emailTable += EmailRow(randomEmailID, id, username, email.dateOf, email.header, email.body, isTrash = false)

        _ <- destinationEmailTable ++= email.to.getOrElse(Seq(EmptyString)).distinct
          .map(DestinationEmailRow(randomEmailID, _, Destination.ToAddress, isTrash = false))

        _ <- destinationEmailTable ++= email.CC.getOrElse(Seq(EmptyString)).distinct
          .map(DestinationEmailRow(randomEmailID, _, Destination.CC, isTrash = false))

        _ <- destinationEmailTable ++= email.BCC.getOrElse(Seq(EmptyString)).distinct
          .map(DestinationEmailRow(randomEmailID, _, Destination.BCC, isTrash = false))
      } yield id

      db.run(insertEmail.transactionally)
    })
  }

  /**
   * Auxiliary function that supports getEmails and getEmail
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received" and "trash"
   * @return Return different queries taking into account the status
   */

  private def auxGetEmails(userEmail: String, status: String): Query[EmailTable, EmailRow, Seq] = {
    status match {
      case EndPointTrash =>
        val queryTrashEmailIds = emailTable
          .filter(_.fromAddress === userEmail)
          .filter(_.isTrash === true)
          .map(_.emailID)
          .union(destinationEmailTable
            .filter(_.username === userEmail)
            .filter(_.isTrash === true)
            .map(_.emailID))

        emailTable.filter(_.emailID in queryTrashEmailIds)
          .sortBy(_.dateOf)

      case EndPointSent =>
        emailTable.filter(_.fromAddress === userEmail)
          .filter(_.isTrash === false)
          .sortBy(_.dateOf)

      case EndPointReceived =>
        val queryReceivedEmailIds = destinationEmailTable
          .filter(_.username === userEmail)
          .map(_.emailID)

        emailTable.filter(_.emailID in queryReceivedEmailIds)
          .filter(_.isTrash === false)
          .sortBy(_.dateOf)

      case EndPointNoFilter =>
        val queryTrashEmailIds = emailTable
          .filter(_.fromAddress === userEmail)
          .filter(_.isTrash === false)
          .map(_.emailID)
          .union(destinationEmailTable
            .filter(_.username === userEmail)
            .filter(_.isTrash === false)
            .map(_.emailID))

        emailTable.filter(_.emailID in queryTrashEmailIds)
          .sortBy(_.dateOf)
    }
  }

  /**
   * Function that filter the emails "sent", "received", "trash" and "noFilter"
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received", "trash" and "noFilter"
   * @return List of emailIDs and corresponding header
   */
  def getEmails(userEmail: String, status: String): Future[Seq[MinimalInfoDTO]] = {
    val queryResult = auxGetEmails(userEmail, status)
      .map(emailTable => (emailTable.emailID, emailTable.header))
      .result

    db.run(queryResult)
      .map(seq => seq.map {
        case (emailID, header) => MinimalInfoDTO(emailID, header)
      })
  }
  /**
   * Function that filter the emails, according to their status and emailID
   * (joinLeft and getOrElse is used to embrace the 3 possible status, however
   * join and no getOrElse would be more appropriate for "sent" and "received")
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received", "trash" or "empty"
   * @param emailID Identification the a specific email
   * @return All the details of the email selected
   */
  def getEmail(userEmail: String, status: String, emailID: String): Future[Seq[EmailInfoDTO]] = {

    val queryTos = db.run(destinationEmailTable
      .filter(_.emailID === emailID)
      .filter(_.destination === Destination.ToAddress)
      .map(_.username)
      .result)

    val queryResult = queryTos.map(seqTos =>
      auxGetEmails(userEmail, status)
        .filter(_.emailID === emailID)
        .map(table => (table.chatID, table.fromAddress, table.header, table.body, table.dateOf))
        .result.map(seq => seq.map {
          case (chatID, fromAddress, header, body, dateOf) =>
            EmailInfoDTO(chatID, fromAddress, seqTos, header, body, dateOf)
        }))

    queryResult.flatMap(db.run(_))
  }

  /**
   * It changes the status of an email to trash or out of trash
   * @param userName Identification of user by email
   * @param emailID Identification the a specific email
   * @return
   */
  def changeTrash(userName: String, emailID: String, moveToTrash: Boolean): Future[Int] = {

    val emailQuery = emailTable
      .filter(_.emailID === emailID)
      .filter(_.fromAddress === userName)
      .filter(_.isTrash === !moveToTrash)
      .map(_.isTrash)
      .update(moveToTrash)

    val destinationQuery = destinationEmailTable
      .filter(_.emailID === emailID)
      .filter(_.username === userName)
      .filter(_.isTrash === !moveToTrash)
      .map(_.isTrash)
      .update(moveToTrash)

    for {
      updateEmailResult <- db.run(emailQuery)
      updateDestinationResult <- db.run(destinationQuery)
    } yield updateEmailResult + updateDestinationResult
  }

  /*Draft repositories*/

  /**
   * Inserts a draft in the database
   * @return Generated chat ID
   */
  def insertDraft(username: String, draft: CreateEmailDTO): Future[String] = {

    val draftID = randomUUID().toString

    val insertDraft = for {
      _ <- draftTable += DraftRow(draftID, draft.chatID.getOrElse(""), username, draft.dateOf, draft.header, draft.body, isTrash = false)
      _ <- destinationDraftTable ++= draft.to.getOrElse(Seq("")).map(DestinationDraftRow(draftID, _, Destination.ToAddress))
      _ <- destinationDraftTable ++= draft.CC.getOrElse(Seq("")).map(DestinationDraftRow(draftID, _, Destination.CC))
      _ <- destinationDraftTable ++= draft.BCC.getOrElse(Seq("")).map(DestinationDraftRow(draftID, _, Destination.BCC))
    } yield draftID

    db.run(insertDraft.transactionally)
  }

  /** Function that update a draft deleting all the data related to that draftID replacing it with the new draft*/
  def updateDraft(draft: CreateEmailDTO, username: String, draftID: String): Future[String] = {

    val updateDraft = for {
      _ <- destinationDraftTable
        .filter(_.draftID === draftID)
        .delete

      _ <- draftTable
        .filter(_.draftID === draftID)
        .filter(_.username === username)
        .delete
    } yield insertDraft(username, draft)

    db.run(updateDraft.transactionally).flatten
  }

  /** Function that selects list of drafts through userName*/
  def getDrafts(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]] = {

    val queryDrafts = draftTable
      .filter(_.username === userEmail)
      .filter(_.isTrash === isTrash)
      .sortBy(_.dateOf.reverse)

    val queryResult = queryDrafts
      .map(draftTable => (draftTable.draftID, draftTable.header))
      .result

    db.run(queryResult).map(seq => seq.map {
      case (id, header) => MinimalInfoDTO(id, header)
    })
  }

  /**
   * Function that filter the emails, according to their draftID, if it is trash or not and userEmail
   * @param userEmail Identification of user by email
   * @param isTrash Boolean representing if the email required is trash or not
   * @param draftID Identification the a specific draft
   * @return All the details of the draft selected
   */
  def getDraft(userEmail: String, isTrash: Boolean, draftID: String): Future[Seq[DraftInfoDTO]] = {

    val queryResult = draftTable
      .filter(_.username === userEmail)
      .filter(_.draftID === draftID)
      .filter(_.isTrash === isTrash)

    val queryDestinationResult = destinationDraftTable
      .filter(_.draftID in queryResult.map(_.draftID))

    for {
      toSeq <- db.run(queryDestinationResult
        .filter(_.destination === Destination.ToAddress).map(_.username).result)

      ccSeq <- db.run(queryDestinationResult
        .filter(_.destination === Destination.CC).map(_.username).result)

      bccSeq <- db.run(queryDestinationResult
        .filter(_.destination === Destination.BCC).map(_.username).result)

      draft <- db.run(queryResult
        .map(table => (table.draftID, table.username, table.header, table.body, table.dateOf))
        .result)

    } yield draft.map {
      case (draftID, username, header, body, dateOf) =>
        DraftInfoDTO(draftID, username, toSeq, ccSeq, bccSeq, header, body, dateOf)
    }
  }

  /**
   * Reaches a certain draft and turns it into an email, if it has any To, CC or BCC
   * @param username Identification of user by email
   * @param draftID Identification the a specific email
   * @return returns a Future of String, that is the emailID from the new inserted email
   */
  def takeDraftMakeSent(username: String, draftID: String, listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[String] = {

    val email = for {
      email <- draftTable
        .filter(_.draftID === draftID)
        .map(entry => (entry.chatID, entry.dateOf, entry.header, entry.body))
        .result
        .headOption

      _ <- destinationDraftTable
        .filter(_.draftID === draftID)
        .delete

      _ <- draftTable
        .filter(_.username === username)
        .filter(_.draftID === draftID)
        .delete

    } yield email.map {
      case (chatId, dateOf, header, body) =>
        CreateEmailDTO(Option(chatId), dateOf, header, body, Option(listTos), Option(listBCCs), Option(listCCs))
    }

    //Won´t insert in db because empty string is not a valid Date
    db.run(email.transactionally).flatMap(emailDTO =>
      insertEmail(username, emailDTO.getOrElse(CreateEmailDTO(Option(EmptyString), EmptyString, EmptyString, EmptyString, Option(Seq(EmptyString)), Option(Seq(EmptyString)), Option(Seq(EmptyString))))))

  }

  def destinations(username: String, draftID: String): Future[(Seq[String], Seq[String], Seq[String])] = {

    val auxQuery = destinationDraftTable
      .filter(_.draftID === draftID)

    for {
      listCCs <- db.run(auxQuery
        .filter(_.destination === Destination.CC)
        .map(_.username)
        .result)

      listBCCs <- db.run(auxQuery
        .filter(_.destination === Destination.BCC)
        .map(_.username)
        .result)

      listTos <- db.run(auxQuery
        .filter(_.destination === Destination.ToAddress)
        .map(_.username)
        .result)
    } yield (listTos, listBCCs, listCCs)
  }

  def hasDestination(listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[Boolean] = {
    Future.successful { listCCs.size + listBCCs.size + listTos.size > 0 }
  }

  def moveInOutTrash(userEmail: String, draftID: String, trash: Boolean): Future[Int] = {

    val draftFilter = draftTable
      .filter(_.username === userEmail)
      .filter(_.draftID === draftID)
      .map(_.isTrash)

    db.run(draftFilter.update(trash))
  }
}
