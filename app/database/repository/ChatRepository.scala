package database.repository

import java.util.UUID.randomUUID

import api.dtos.{ CreateEmailDTO, CreateShareDTO }
import com.google.inject.Inject
import database.mappings.ChatMappings._
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.{ ChatRow, ShareRow }
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

//TODO: Reimplement using Trait + Implementation Class instead. Will make Injection and BL/DL separation easier which you currently are tangling a bit.
//Also you don't need to use Injection here.
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
   * Queries to find the inbox messages of an user
   * @param userEmail user email
   * @return All the mails that have the username in "from", "to", "CC" and "BCC" categories
   */
  def showInbox(userEmail: String): Future[Seq[(String, String)]] = {
    val queryEmailIds = emailTable.filter(_.fromAddress === userEmail).map(_.emailID)
      .union(toAddressTable.filter(_.username === userEmail).map(_.emailID))
      .union(ccTable.filter(_.username === userEmail).map(_.emailID))
      .union(bccTable.filter(_.username === userEmail).map(_.emailID))
    val queryResult2 = emailTable
      .filter(_.emailID in queryEmailIds)
      .filter(_.sent === true)
      .sortBy(_.dateOf)
      .map(x => (x.chatID, x.header))
      .result
    db.run(queryResult2)
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
}
