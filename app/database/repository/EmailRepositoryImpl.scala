
package database.repository

import java.util.UUID.randomUUID

import api.dtos.{ CreateEmailDTO, EmailInfoDTO, MinimalInfoDTO }
import database.mappings.EmailMappings.{ emailTable, _ }
import database.mappings.EmailMappings._
import database.mappings._
import definedStrings.ApiStrings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

/**  Class that receives a db path */
class EmailRepositoryImpl @Inject() (implicit val executionContext: ExecutionContext, db: Database, chatActions: ChatRepositoryImpl) extends EmailRepository {

  /**
   * Inserts an email in the database
   * @return Generated chat ID
   */
  def insertEmail(username: String, email: CreateEmailDTO): Future[String] = {
    val randomEmailID = randomUUID().toString

    val chatId = chatActions.insertChat(email, email.chatID.getOrElse(randomUUID().toString))
    chatId.flatMap(id => {
      val insertEmail = for {
        _ <- emailTable += EmailRow(randomEmailID, id, username, email.dateOf, email.header, email.body, isTrash = false)

        _ <- destinationEmailTable ++= email.to.getOrElse(Seq("")).distinct
          .map(DestinationEmailRow(randomEmailID, _, Destination.ToAddress, isTrash = false))

        _ <- destinationEmailTable ++= email.CC.getOrElse(Seq("")).distinct
          .map(DestinationEmailRow(randomEmailID, _, Destination.CC, isTrash = false))

        _ <- destinationEmailTable ++= email.BCC.getOrElse(Seq("")).distinct
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
        val queryTrashEmailIds = emailTable.filter(_.fromAddress === userEmail).filter(_.isTrash === true).map(_.emailID)
          .union(destinationEmailTable.filter(_.username === userEmail).filter(_.isTrash === true).map(_.emailID))

        emailTable.filter(_.emailID in queryTrashEmailIds)
          .sortBy(_.dateOf)

      case EndPointSent =>
        emailTable.filter(_.fromAddress === userEmail)
          .filter(_.isTrash === false)
          .sortBy(_.dateOf)

      case EndPointReceived =>
        val queryReceivedEmailIds = destinationEmailTable.filter(_.username === userEmail).map(_.emailID)

        emailTable.filter(_.emailID in queryReceivedEmailIds)
          .filter(_.isTrash === false)
          .sortBy(_.dateOf)

      case EndPointNoFilter =>
        emailTable.filter(_.fromAddress === userEmail)
          .filter(_.isTrash === false)
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
    db.run(queryResult).map(seq => seq.map {
      case (emailId, header) =>
        MinimalInfoDTO(emailId, header)
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

    val seqTos = db.run(destinationEmailTable.filter(_.emailID === emailID).filter(_.destination === Destination.ToAddress).map(_.username).result)

    val queryResult = seqTos.map(x => auxGetEmails(userEmail, status)
      .filter(_.emailID === emailID)
      .map(table => (table.chatID, table.fromAddress, table.header, table.body, table.dateOf))
      .result.map(seq => seq.map {
        case (chatID, fromAddress, header, body, dateOf) =>
          EmailInfoDTO(chatID, fromAddress, x, header, body, dateOf)
      }))
    queryResult.flatMap(db.run(_))
  }

  private def hasSenderAddress(to: Option[Seq[String]]): Boolean = {
    to.getOrElse(Seq()).nonEmpty
  }

  /**
   * It changes the status of an email to trash or out of trash
   * @param userName Identification of user by email
   * @param emailID Identification the a specific email
   * @return
   */
  def changeTrash(userName: String, emailID: String): Future[Int] = {
    val filteredEmailTable = emailTable.filter(_.emailID === emailID).filter(_.fromAddress === userName).map(_.isTrash)
    val currentEmailStatus = db.run(filteredEmailTable.result.headOption)

    val filteredToAddressTable = destinationEmailTable.filter(_.destination === Destination.ToAddress)
      .filter(_.emailID === emailID).filter(_.username === userName).map(_.isTrash)

    val currentToStatus = db.run(filteredToAddressTable.result.headOption)

    val filteredCCAddressTable = destinationEmailTable.filter(_.destination === Destination.CC)
      .filter(_.emailID === emailID).filter(_.username === userName).map(_.isTrash)

    val currentCCStatus = db.run(filteredCCAddressTable.result.headOption)

    val filteredBCCAddressTable = destinationEmailTable.filter(_.destination === Destination.BCC)
      .filter(_.emailID === emailID).filter(_.username === userName).map(_.isTrash)

    val currentBCCStatus = db.run(filteredBCCAddressTable.result.headOption)

    for {
      resultEmailTable <- currentEmailStatus.map(status =>
        filteredEmailTable.update(!status.getOrElse(true))).flatMap(db.run(_))

      resultToAddressTable <- currentToStatus.map(status =>
        filteredToAddressTable.update(!status.getOrElse(true))).flatMap(db.run(_))

      resultCCTable <- currentCCStatus.map(status =>
        filteredCCAddressTable.update(!status.getOrElse(true))).flatMap(db.run(_))

      resultBCCTable <- currentBCCStatus.map(status =>
        filteredBCCAddressTable.update(!status.getOrElse(true))).flatMap(db.run(_))

    } yield resultEmailTable + resultToAddressTable + resultCCTable + resultBCCTable
  }
}
