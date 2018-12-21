package database.repository

import java.util.UUID.randomUUID

import api.dtos.{ CreateEmailDTO, CreateShareDTO }
import database.mappings.ChatMappings._
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.{ ChatRow, ShareRow }
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

//TODO: Reimplement using Trait + Implementation Class instead. Will make Injection and BL/DL separation easier which you currently are tangling a bit.
class ChatRepository @Inject() (implicit val executionContext: ExecutionContext, implicit val db: Database) {
  /**
   * Insert a chat into database
   * @param email email passed on json body
   * @param chatID chatID
   * @return
   */
  def insertChat(email: CreateEmailDTO, chatID: String): Future[String] = {
    val randomChatID = randomUUID().toString
    existChatID(chatID).map {
      case true => chatID
      case false =>
        db.run(chatTable += ChatRow(randomChatID, email.header))
        randomChatID
    }
  }

  /**
   * Aims to find an chatID already exists in the database
   * @param chatID Reference to an email conversation
   * @return True or False depending if the chatID exists or not
   */
  def existChatID(chatID: String): Future[Boolean] = {
    val tableSearch = chatTable.filter(_.chatID === chatID).result
    db.run(tableSearch).map(_.length).map {
      case 1 => true
      case _ => false
    }
  }

  /**
   * Query that search for all the emails which have a certain userName involved
   * @param userEmail the user identity
   * @return The sequence of emailIDS which userEmail is involved (to, from cc and bcc)
   */
  def queryEmailIds(userEmail: String) = {
    emailTable.filter(_.fromAddress === userEmail).map(_.emailID)
      .union(toAddressTable.filter(_.username === userEmail).map(_.emailID))
      .union(ccTable.filter(_.username === userEmail).map(_.emailID))
      .union(bccTable.filter(_.username === userEmail).map(_.emailID))
  }
  /**
   * Queries to find the inbox messages of an user
   * @param userEmail user email
   * @return All the mails that have the username in "from", "to", "CC" and "BCC" categories
   */
  def showInbox(userEmail: String): Future[Seq[(String, String)]] = {
    val queryuserName = queryEmailIds(userEmail)
    val queryResult = emailTable
      .filter(_.emailID in queryuserName)
      .filter(_.sent === true)
      .sortBy(_.dateOf)
      .map(x => (x.chatID, x.header))
      .result
    db.run(queryResult)
  }

  /**
   * Query that selects the emailIDs from the EmailTable that
   * are returned by the auxiliary query "queryuserName", filters by chatID inputed,
   * by the state "Sent", and sort by the date.
   * @return query result
   */
  def querychatID(userEmail: String, chatID: String) = {
    val queryuserName = queryEmailIds(userEmail)
    emailTable
      .filter(_.emailID in queryuserName)
      .filter(_.chatID === chatID)
      .filter(_.sent === true)
      .sortBy(_.dateOf)
  }

  /**
   *
   * @param userEmail
   * @param chatID
   * @return
   */
  def getEmails(userEmail: String, chatID: String): Future[Seq[(String, String)]] = {
    val queryResult = querychatID(userEmail, chatID)
      .map(x => (x.emailID, x.header))
      .result
    db.run(queryResult)
  }

  /**
   *
   * @param userEmail
   * @param chatID
   * @param emailID
   * @return
   */
  def getEmailID(userEmail: String, chatID: String, emailID: String) = {
    val queryResult = querychatID(userEmail, chatID)
      .filter(_.emailID === emailID)
      //Since every email with sent==true is obligated to have an ToID,
      // the following join has the same effect as joinleft
      .join(toAddressTable).on(_.emailID === _.emailID)
      //Order of the following map: fromAddress, username(from toAddress table), header, body,  dateOf
      .map(x => (x._1.fromAddress, x._2.username, x._1.header, x._1.body, x._1.dateOf))
      .result
    db.run(queryResult)
  }
  /**
   *  Authorize an user to have access to a conversation
   * @param from User that concedes permission
   * @param share User that grants the authorization
   * @return Insert permission to an user
   */
  def insertPermission(from: String, share: CreateShareDTO): Future[String] = {
    val shareID = randomUUID().toString
    db.run(shareTable += ShareRow(shareID, share.chatID, from, share.supervisor))
    Future { shareID }
  }

  def getSharesChats(userEmail: String) = {

    val queryEmailIds = shareTable.filter(_.toID === userEmail).map(x => (x.chatID, x.fromUser))
    val queryFromUser = emailTable.filter(_.fromAddress in queryEmailIds.map(x => x._2)).map(_.emailID)
      .union(toAddressTable.filter(_.username in queryEmailIds.map(x => x._2)).map(_.emailID))
      .union(ccTable.filter(_.username in queryEmailIds.map(x => x._2)).map(_.emailID))
      .union(bccTable.filter(_.username in queryEmailIds.map(x => x._2)).map(_.emailID))
    val queryChatID = emailTable.filter(_.chatID in queryEmailIds.map(x => x._1))
      .filter(_.emailID in queryFromUser)
      .sortBy(_.dateOf)
      .map(x => (x.chatID, x.header)).result
    db.run(queryChatID)

  }

  def deletePermission(from: String, to: String, chatID: String) = {
    val deletePermissionTable = shareTable.filter(p => (p.fromUser === from) && (p.toID === to) && (p.chatID === chatID)).delete
    db.run(deletePermissionTable)
  }
}
