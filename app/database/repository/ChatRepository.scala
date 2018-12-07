package database.repository

import api.dto.{ CreateEmailDTO, CreateUserDTO }
import database.mappings.{ Chat, User }
import database.mappings.ChatMappings.ChatTable
import database.mappings.UserMappings.UserTable
import encryption.EncryptString
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

class ChatRepository {
  //val db = Database.forConfig(path)

  def insertChat(email: CreateEmailDTO, random: String) = {
    ChatTable += Chat(email.chatID.getOrElse(random), email.header)
  }
}
