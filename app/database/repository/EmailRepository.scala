
package database.repository

import java.util.UUID.randomUUID

import api.dtos.CreateEmailDTO
import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.mappings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._
import definedStrings.ApiStrings._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Class that receives a db path
 */
class EmailRepository @Inject() (implicit val executionContext: ExecutionContext, implicit val db: Database, chatActions: ChatRepository) {

  /**
   * Inserts an email in the database
   * @return Generated email ID
   */
  def insertEmail(username: String, email: CreateEmailDTO): Future[String] = {
    val randomEmailID = randomUUID().toString
    val chatID = chatActions.insertChat(email, email.chatID.getOrElse(randomUUID().toString))

    val insertEmailTable = chatID.map(emailTable += EmailRow(randomEmailID, _, username, email.dateOf, email.header, email.body,
      if (hasSenderAddress(email.to)) { email.sendNow } else { false }))
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
   * Queries that filter the emails "sent", "received", "draft" and "supervised"
   * @param userEmail User logged email
   * @param status cathegory of emails wanted
   * @return List of emails
   */
  def showEmails(userEmail: String, status: String): Future[Seq[(String, String)]] = {
    status match {
      case EndPointSent =>
        val querySentEmailIds = emailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === true)
          .sortBy(_.dateOf)
          .map(x => (x.emailID, x.header)).result
        db.run(querySentEmailIds)

      case EndPointReceived =>
        val queryReceivedEmailIds = toAddressTable
          .filter(_.username === userEmail).map(_.emailID)
          .union(ccTable.filter(_.username === userEmail).map(_.emailID))
          .union(bccTable.filter(_.username === userEmail).map(_.emailID))
        val queryReceivedEmailIdsAux = emailTable.filter(_.emailID in queryReceivedEmailIds)
          .filter(_.sent === true)
          .sortBy(_.dateOf)
          .map(x => (x.emailID, x.header))
          .result
        db.run(queryReceivedEmailIdsAux)

      case EndPointDraft =>
        val querySentEmailIds = emailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === false)
          .sortBy(_.dateOf)
          .map(x => (x.emailID, x.header))
          .result
        db.run(querySentEmailIds)
    }
  }

  def getEmail(userEmail: String, status: String, emailID: String) = {
    status match {
      case EndPointSent =>
        val querySentEmailIds = emailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === true)
          .sortBy(_.dateOf)
          .filter(_.emailID === emailID)
          .join(toAddressTable).on(_.emailID === _.emailID)
          .map(x => (x._1.chatID, x._1.fromAddress, x._2.username, x._1.header, x._1.body, x._1.dateOf))
          .result
        db.run(querySentEmailIds)

      case EndPointReceived =>
        val queryReceivedEmailIds = toAddressTable
          .filter(_.username === userEmail).map(_.emailID)
          .union(ccTable.filter(_.username === userEmail).map(_.emailID))
          .union(bccTable.filter(_.username === userEmail).map(_.emailID))
        val queryReceivedEmailIdsAux = emailTable.filter(_.emailID in queryReceivedEmailIds)
          .filter(_.sent === true)
          .filter(_.emailID === emailID)
          .sortBy(_.dateOf)
          .join(toAddressTable).on(_.emailID === _.emailID)
          .map(x => (x._1.chatID, x._1.fromAddress, x._2.username, x._1.header, x._1.body, x._1.dateOf))
          .result
        db.run(queryReceivedEmailIdsAux)

      case EndPointDraft =>
        val querySentEmailIds = emailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === false)
          .filter(_.emailID === emailID)
          .sortBy(_.dateOf)
          .joinLeft(toAddressTable).on(_.emailID === _.emailID)
          .map(x => (x._1.chatID, x._1.fromAddress, x._2.map(_.username).getOrElse(EmptyString), x._1.header, x._1.body, x._1.dateOf))
          .result
        db.run(querySentEmailIds)
    }
  }

  private def hasSenderAddress(to: Option[Seq[String]]): Boolean = {
    to.getOrElse(Seq()).nonEmpty
  }
}
