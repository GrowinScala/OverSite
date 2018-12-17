package database.repository

import java.util.UUID.randomUUID

import api.dto.{ CreateEmailDTO, CreateShareDTO }
import database.mappings.ChatMappings._
import database.mappings.EmailMappings.{ BCCTable, CCTable, EmailTable, ToAddressTable }
import database.mappings.{ Chat, Share }
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

class ChatRepository(path: String)(implicit val executionContext: ExecutionContext) {
  val db = Database.forConfig(path)

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

  def existChatID(chatID: String): Future[Boolean] = {
    val tableSearch = ChatTable.filter(_.chatID === chatID).result
    db.run(tableSearch).map(_.length).map {
      case 1 => true
      case _ => false
    }
  }

  def showInbox(userEmail: String): Future[Seq[(String, String)]] = {
    val queryEmailIds = EmailTable.filter(_.fromAddress === userEmail).map(_.emailID)
      .union(ToAddressTable.filter(_.username === userEmail).map(_.emailID))
      .union(CCTable.filter(_.username === userEmail).map(_.emailID))
      .union(BCCTable.filter(_.username === userEmail).map(_.emailID))
    val queryResult2 = EmailTable
      .filter(_.emailID in queryEmailIds)
      .filter(_.sent === true)
      .sortBy(_.dateOf)
      .map(x => (x.chatID, x.header))
      .result
    db.run(queryResult2)
  }

  def insertPermission(from: String, share: CreateShareDTO): Future[String] = {
    val shareID = randomUUID().toString
    db.run(ShareTable += Share(shareID, share.chatID, from, share.supervisor))
    Future { shareID }
  }
}
