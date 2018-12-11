package database.repository

import java.util.UUID.randomUUID

import api.dto.CreateEmailDTO
import database.mappings.Chat
import database.mappings.ChatMappings.ChatTable
import slick.ast.Select
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext

class ChatRepository(path: String)(implicit val executionContext: ExecutionContext) {
  val db = Database.forConfig(path)

  /**
   * Insert a chat into database
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

  def existChatID(chatID: String) = {
    val tableSearch = ChatTable.filter(_.chatID === chatID).result
    db.run(tableSearch).map(_.length).map {
      case 1 => true
      case _ => false
    }
  }

  /*def showInbox(userName: String, token: String): Unit ={
    Select from ChatTable
  }*/
}
