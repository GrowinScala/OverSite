package database.repository

import java.util.UUID.randomUUID

import api.dto.CreateUserDTO
import com.google.inject.AbstractModule
import database.mappings.UserMappings._
import database.mappings.{ Login, User }
import encryption.EncryptString
import com.google.inject.Inject
import play.api.inject.Module
import play.inject.Module
import slick.basic.DatabaseConfig
import slick.jdbc.MySQLProfile
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Future
/**
 * Class that receives a db path
 */
class UserRepository @Inject() (db: Database) {

  /**
   * Insert an user into database with is password encrypted
   * @return The number of insertions into database
   */
  def insertUser(user: CreateUserDTO): Future[Int] = {
    val encrypt = new EncryptString(user.password, "MD5")
    val insertTableEmail = UserTable += User(user.username, encrypt.result.toString)
    db.run(insertTableEmail)
  }

  /**
   * Logins an user, once this provides a matching username and password
   */
  def loginUser(user: CreateUserDTO): Future[Seq[User]] = {
    val encrypt = new EncryptString(user.password, "MD5")
    val realUser = UserTable.filter(x => (x.username === user.username) && x.password === encrypt.result.toString).result
    db.run(realUser)
  }

  /**
   * Inserts a login into database with the expire token session as the current server time plus 1 hour
   * @return Generated token
   */
  def insertLogin(user: CreateUserDTO): String = {
    val token = randomUUID().toString
    val insertTableLogin = LoginTable += Login(user.username, token, validate1Hour)

    db.run(insertTableLogin)
    token
  }

  /**
   * Validates the token for 1 hour
   * @return Current server time plus 1 hour
   */
  def validate1Hour: Long = {
    val currentTime = System.currentTimeMillis()
    val valid1Hour = currentTime + 3600000
    valid1Hour
  }

  /**
   * Validates the userName and token inserted by the user
   */
  def validateToken(userName: String, token: String): Future[Seq[Login]] = {
    val validateTableToken = LoginTable.filter(x => (x.username === userName) && (x.token === token) && x.validDate > System.currentTimeMillis()).result
    db.run(validateTableToken)
  }
}
