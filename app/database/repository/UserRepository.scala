package database.repository

import api.dto.CreateUserDTO
import database.mappings.User
import database.mappings.UserMappings._
import encryption.EncryptString
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future

class UserRepository(path: String) {
  val db = Database.forConfig(path)

  def insertUser(user: CreateUserDTO): Future[Int] = {
    val encrypt = new EncryptString(user.password)
    val insertTableEmail = UserTable += User(user.username, encrypt.result.toString)
    db.run(insertTableEmail)
  }
}
