package database.repository

import java.util.UUID.randomUUID

import api.dto.{ CreateChatDTO, CreateEmailDTO }
import database.mappings.{ Chat, ChatTable }
import database.mappings.ChatMappings.ChatTable
import database.mappings.EmailMappings.{ BCCTable, CCTable, EmailTable, ToAddressTable }
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

class ChatRepository(path: String)(implicit val executionContext: ExecutionContext) {
  val db = Database.forConfig(path)

  /**
   * Insert a chat into database
   *
   * @param email
   * @param random
   * @return
   */
  def insertChat(email: CreateEmailDTO, chatID: String) = {
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

  def showInbox(userEmail: String) = {
    val queryEmailIds = EmailTable.filter(_.fromAddress === userEmail).map(_.emailID)
      .union(ToAddressTable.filter(_.username === userEmail).map(_.emailID))
      .union(CCTable.filter(_.username === userEmail).map(_.emailID))
      .union(BCCTable.filter(_.username === userEmail).map(_.emailID))
    val queryResult2 = EmailTable.filter(_.emailID in queryEmailIds).filter(_.sent === true).sortBy(_.dateOf)
      .map(x => (x.chatID, x.header)).result
    db.run(queryResult2)
  }


}
