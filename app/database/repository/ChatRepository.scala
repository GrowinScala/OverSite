package database.repository

import java.util.UUID.randomUUID

import api.dto.{ CreateEmailDTO, CreateShareDTO }
import com.google.inject.Inject
import database.mappings.ChatMappings._
import database.mappings.EmailMappings.{ BCCTable, CCTable, EmailTable, ToAddressTable }
import database.mappings.{ Chat, Share }
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

class ChatRepository @Inject() (db: Database)(implicit val executionContext: ExecutionContext) {
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
        db.run(ChatTable += Chat(randomChatID, email.header))
        randomChatID
    }
  }

  /**
   * Aims to find an chatID already exists in the database
   * @param chatID Reference to an email conversation
   * @return True or False depending if the chatID exists or not
   */
  def existChatID(chatID: String): Future[Boolean] = {
    val tableSearch = ChatTable.filter(_.chatID === chatID).result
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
    EmailTable.filter(_.fromAddress === userEmail).map(_.emailID)
      .union(ToAddressTable.filter(_.username === userEmail).map(_.emailID))
      .union(CCTable.filter(_.username === userEmail).map(_.emailID))
      .union(BCCTable.filter(_.username === userEmail).map(_.emailID))
  }
  /**
   * Queries to find the inbox messages of an user
   * @param userEmail user email
   * @return All the mails that have the username in "from", "to", "CC" and "BCC" categories
   */
  def showInbox(userEmail: String): Future[Seq[(String, String)]] = {
    val queryuserName = queryEmailIds(userEmail)
    val queryResult = EmailTable
      .filter(_.emailID in queryuserName)
      .filter(_.sent === true)
      .sortBy(_.dateOf)
      .map(x => (x.chatID, x.header))
      .result
    db.run(queryResult)
  }

  def getEmails(userEmail: String, chatID: String): Future[Seq[(String, String)]] = {
    val queryuserName = queryEmailIds(userEmail)
    val queryResult = EmailTable
      .filter(_.emailID in queryuserName)
      .filter(_.chatID === chatID)
      .filter(_.sent === true)
      .sortBy(_.dateOf)
      .map(x => (x.emailID, x.header))
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
    db.run(ShareTable += Share(shareID, share.chatID, from, share.supervisor))
    Future { shareID }
  }
}
