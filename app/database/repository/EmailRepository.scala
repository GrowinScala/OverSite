
package database.repository

import java.util.UUID.randomUUID

import api.dto.CreateEmailDTO
import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.mappings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Class that receives a db path
  */
class EmailRepository @Inject() (db: Database)(implicit val executionContext: ExecutionContext) {

  /**
    * Inserts an email in the database
    * @return Generated email ID
    */
  def insertEmail(username: String, email: CreateEmailDTO): Future[String] = {
    val randomEmailID = randomUUID().toString
    val chatActions = new ChatRepository(db)
    val chatID = chatActions.insertChat(email, email.chatID.getOrElse(randomUUID().toString))

    val insertEmailTable = chatID.map(EmailTable += Email(randomEmailID, _, username, email.dateOf, email.header, email.body,
      if (hasSenderAddress(email.to)) { email.sendNow } else { false }))
    val insertAddressTable = ToAddressTable ++= email.to.getOrElse(Seq()).map(ToAddress(randomUUID().toString, randomEmailID, _))
    val insertCCTable = CCTable ++= email.CC.getOrElse(Seq()).map(CC(randomUUID().toString, randomEmailID, _))
    val insertBCCTable = BCCTable ++= email.BCC.getOrElse(Seq()).map(BCC(randomUUID().toString, randomEmailID, _))

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
      case "sent" =>
        val querySentEmailIds = EmailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === true)
          .sortBy(_.dateOf)
          .map(x => (x.emailID, x.header)).result
        db.run(querySentEmailIds)

      case "received" =>
        val queryReceivedEmailIds = ToAddressTable
          .filter(_.username === userEmail).map(_.emailID)
          .union(CCTable.filter(_.username === userEmail).map(_.emailID))
          .union(BCCTable.filter(_.username === userEmail).map(_.emailID))
        val queryReceivedEmailIdsAux = EmailTable.filter(_.emailID in queryReceivedEmailIds)
          .filter(_.sent === true)
          .sortBy(_.dateOf)
          .map(x => (x.emailID, x.header))
          .result
        db.run(queryReceivedEmailIdsAux)

      case "draft" =>
        val querySentEmailIds = EmailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === false)
          .sortBy(_.dateOf)
          .map(x => (x.emailID, x.header)).result
        db.run(querySentEmailIds)

      case "supervised" =>
        val queryEmailIds = ShareTable.filter(_.toID === userEmail).map(x => (x.chatID, x.fromUser))
        val queryFromUser = EmailTable.filter(_.fromAddress in queryEmailIds.map(x => x._2)).map(_.emailID)
          .union(ToAddressTable.filter(_.username in queryEmailIds.map(x => x._2)).map(_.emailID))
          .union(CCTable.filter(_.username in queryEmailIds.map(x => x._2)).map(_.emailID))
          .union(BCCTable.filter(_.username in queryEmailIds.map(x => x._2)).map(_.emailID))
        val queryChatID = EmailTable.filter(_.chatID in queryEmailIds.map(x => x._1))
          .filter(_.emailID in queryFromUser)
          .sortBy(_.dateOf)
          .map(x => (x.chatID, x.header)).result
        db.run(queryChatID)

    }
  }

  def hasSenderAddress(to: Option[Seq[String]]): Boolean = {
    to.getOrElse(Seq()).nonEmpty
  }
}
