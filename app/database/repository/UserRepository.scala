package database.repository

import java.util.UUID.randomUUID

import api.dto.CreateUserDTO
import database.mappings.UserMappings._
import database.mappings.{ Login, User }
import encryption.EncryptString
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Class that receives a db path
 * @param path db configuration path
 */
class UserRepository(path: String) {
  /**
   * Sets a database using target path configuration
   */
  val db = Database.forConfig(path)

  /**
   * Insert an user into database with is password encrypted
   * @param user user received from json body
   * @return The number of insertions into database
   */
  def insertUser(user: CreateUserDTO): Future[Int] = {
    val encrypt = new EncryptString(user.password, "MD5")
    val insertTableEmail = UserTable += User(user.username, encrypt.result.toString)
    db.run(insertTableEmail)
  }

  /**
   * Logins an user, once this provides a matching username and password
   * @param user user received from json body
   * @return
   */
  def loginUser(user: CreateUserDTO): Future[Seq[User]] = {
    val encrypt = new EncryptString(user.password, "MD5")
    val realUser = UserTable.filter(x => (x.username === user.username) && x.password === encrypt.result.toString).result
    db.run(realUser)
  }

  /**
   * Inserts a login into database with the expire token session as the current server time plus 1 hour
   * @param user user received from json body
   * @return generated token
   */
  def insertLogin(user: CreateUserDTO): String = {
    val token = randomUUID().toString
    val insertTableLogin = LoginTable += Login(user.username, token, validate1Hour)

    db.run(insertTableLogin)
    token
  }

  /**
   *
   * @return current server time plus 1 hour
   */
  def validate1Hour: Long = {
    val currentTime = System.currentTimeMillis()
    val valid1Hour = currentTime + 3600000
    valid1Hour
  }

  /**
   * Validates the userName and token inserted by the user
   * @param userName username passed from api call
   * @param token token received from json header
   * @return
   */
  def validateToken(userName: String, token: String): Future[Seq[Login]] = {
    val validateTableToken = LoginTable.filter(x => (x.username === userName) && (x.token === token) && x.validDate > System.currentTimeMillis()).result
    db.run(validateTableToken)
  }

  def getUserByToken(token: String)(implicit exec: ExecutionContext): Future[String] = {
    val getUser = LoginTable.filter(_.token === token).map(_.username).result
    db.run(getUser).map(_.head)
  }
}
