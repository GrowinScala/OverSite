
package database.repository

import java.util.UUID.randomUUID

import api.dtos.CreateEmailDTO
import database.mappings.EmailMappings._
import database.mappings._
import definedStrings.ApiStrings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

/**  Class that receives a db path */
class EmailRepository @Inject() (implicit val executionContext: ExecutionContext, db: Database, chatActions: ChatRepository) {

  /**
   * Inserts an email in the database
   * @return Generated email ID
   */
  def insertEmail(username: String, email: CreateEmailDTO): Future[String] = {
    val randomEmailID = randomUUID().toString
    val chatID = chatActions.insertChat(email, email.chatID.getOrElse(randomUUID().toString))

    val insertEmailTable = chatID.map(emailTable += EmailRow(randomEmailID, _, username, email.dateOf, email.header, email.body,
      if (hasSenderAddress(email.to))
        email.sendNow
      else
        false))

    val insertAddressTable = toAddressTable ++= email.to.getOrElse(Seq()).map(ToAddressRow(randomUUID().toString, randomEmailID, _))
    val insertCCTable = ccTable ++= email.CC.getOrElse(Seq()).map(CCRow(randomUUID().toString, randomEmailID, _))
    val insertBCCTable = bccTable ++= email.BCC.getOrElse(Seq()).map(BCCRow(randomUUID().toString, randomEmailID, _))

    insertEmailTable.map(db.run(_))
    db.run(insertAddressTable)
    db.run(insertCCTable)
    db.run(insertBCCTable)
    chatID
  }

  /**
   * Auxiliary function that supports getEmails and getEmail
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received" and "draft"
   * @return Return different queries taking into account the status
   */
  def auxGetEmails(userEmail: String, status: String) = {
    status match {
      case EndPointSent =>
        emailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === true)
          .sortBy(_.dateOf)

      case EndPointReceived =>
        val queryReceivedEmailIds = toAddressTable
          .filter(_.username === userEmail).map(_.emailID)
          .union(ccTable.filter(_.username === userEmail).map(_.emailID))
          .union(bccTable.filter(_.username === userEmail).map(_.emailID))

        emailTable.filter(_.emailID in queryReceivedEmailIds)
          .filter(_.sent === true)
          .sortBy(_.dateOf)

      case EndPointDraft =>
        emailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === false)
          .sortBy(_.dateOf)
    }
  }

  /**
   * Function that filter the emails "sent", "received", "draft" and "supervised"
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received" and "draft"
   * @return List of emailIDs and corresponding header
   */
  def getEmails(userEmail: String, status: String): Future[Seq[(String, String)]] = {
    val queryResult = auxGetEmails(userEmail, status)
      .map(x => (x.emailID, x.header))
      .result
    db.run(queryResult)
  }
  /**
   * Function that filter the emails, according to their status and emailID
   * (joinLeft and getOrElse is used to embrace the 3 possible status, however
   * join and no getOrElse would be more appropriate for "sent" and "received")
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received" and "draft"
   * @param emailID Identification the a specific email
   * @return All the details of the email selected
   */
  def getEmail(userEmail: String, status: String, emailID: String) = {
    val queryResult = auxGetEmails(userEmail, status)
      .filter(_.emailID === emailID)
      .joinLeft(toAddressTable).on(_.emailID === _.emailID)
      .map(x => (x._1.chatID, x._1.fromAddress, x._2.map(_.username).getOrElse(EmptyString), x._1.header, x._1.body, x._1.dateOf))
      .result
    db.run(queryResult)
  }

  private def hasSenderAddress(to: Option[Seq[String]]): Boolean = {
    to.getOrElse(Seq()).nonEmpty
  }

  def takeDraftMakeSent(userName: String, emailID: String) = {

    val hasToAddress = toAddressTable.filter(_.emailID === emailID).result

    val toSent = emailTable.filter(p => (p.emailID === emailID) && (p.fromAddress === userName))
      .map(_.sent)
      .update(true)

    db.run(hasToAddress).map(_.length).flatMap {
      case 1 => db.run(toSent)
      case _ => Future { 0 }
    }
  }
}
