
package database.repository

import java.util.UUID.randomUUID

import api.dtos.{ CreateEmailDTO, EmailInfoDTO, MinimalInfoDTO }
import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ emailTable, _ }
import database.mappings._
import definedStrings.ApiStrings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

/**  Class that receives a db path */
class EmailRepositoryImpl @Inject() (implicit val executionContext: ExecutionContext, db: Database) extends EmailRepository {

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
}
