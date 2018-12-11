package database.repository

import api.dto.CreateEmailDTO
import database.mappings.Chat
import database.mappings.ChatMappings.ChatTable
import slick.jdbc.MySQLProfile.api._

class ChatRepository {
  //val db = Database.forConfig(path)

  /**
    * Insert a chat into database
    * @param email
    * @param random
    * @return
    */
  def insertChat(email: CreateEmailDTO, random: String) = {
    ChatTable += Chat(email.chatID.getOrElse(random), email.header)
  }
}
