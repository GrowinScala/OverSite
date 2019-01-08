
package database.repository

import java.util.UUID.randomUUID

import api.dtos.{ CreateEmailDTO, EmailInfoDTO, EmailMinimalInfoDTO }
import database.mappings.EmailMappings.{ emailTable, _ }
import database.mappings._
import definedStrings.ApiStrings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Success

/**  Class that receives a db path */
class EmailRepositoryImpl @Inject() (implicit val executionContext: ExecutionContext, db: Database, chatActions: ChatRepositoryImpl) extends EmailRepository {

  /**
   * Inserts an email in the database
   * @return Generated chat ID
   */
  def insertEmail(username: String, email: CreateEmailDTO): Future[String] = {
    val randomEmailID = randomUUID().toString
    val isDraft = if (hasSenderAddress(email.to)) email.sendNow
    else false

    val chatId = chatActions.insertChat(email, email.chatID.getOrElse(randomUUID().toString))
    chatId.flatMap(id => {
      val insertEmail = for {
        _ <- emailTable += EmailRow(randomEmailID, id, username, email.dateOf, email.header, email.body, isDraft)
        _ <- toAddressTable ++= email.to.getOrElse(Seq()).map(ToAddressRow(randomUUID().toString, randomEmailID, _))
        _ <- ccTable ++= email.CC.getOrElse(Seq()).map(CCRow(randomUUID().toString, randomEmailID, _))
        _ <- bccTable ++= email.BCC.getOrElse(Seq()).map(BCCRow(randomUUID().toString, randomEmailID, _))
      } yield id

      db.run(insertEmail.transactionally)
    })
  }

  /**
   * Auxiliary function that supports getEmails and getEmail
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received" and "draft"
   * @return Return different queries taking into account the status
   */
  private def auxGetEmails(userEmail: String, status: String) = {
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
  def getEmails(userEmail: String, status: String): Future[Seq[EmailMinimalInfoDTO]] = {
    val queryResult = auxGetEmails(userEmail, status)
      .map(x => (x.emailID, x.header))
      .result
    db.run(queryResult).map(seq => seq.map(p => EmailMinimalInfoDTO(p._1, p._2)))
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
  def getEmail(userEmail: String, status: String, emailID: String): Future[Seq[EmailInfoDTO]] = {
    val queryResult = auxGetEmails(userEmail, status)
      .filter(_.emailID === emailID)
      .joinLeft(toAddressTable).on(_.emailID === _.emailID)
      .map(x => (x._1.chatID, x._1.fromAddress, x._2.map(_.username).getOrElse(EmptyString), x._1.header, x._1.body, x._1.dateOf))
      .result

    db.run(queryResult).map(seq => seq.map(p => EmailInfoDTO(p._1, p._2, p._3, p._4, p._5, p._6)))
  }

  private def hasSenderAddress(to: Option[Seq[String]]): Boolean = {
    to.getOrElse(Seq()).nonEmpty
  }

  def takeDraftMakeSent(userName: String, emailID: String): Future[Int] = {

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
