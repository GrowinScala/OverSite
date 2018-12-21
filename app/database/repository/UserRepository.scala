package database.repository

import java.util.UUID.randomUUID

import api.dtos.CreateUserDTO
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import encryption.EncryptString
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._
import definedStrings.AlgorithmStrings._

import scala.concurrent.{ ExecutionContext, Future }
/**
 * Class that receives a db path
 */
class UserRepository @Inject() (implicit val executionContext: ExecutionContext, implicit val db: Database) {

  /**
   * Insert an user into database with is password encrypted
   * @return The number of insertions into database
   */
  def insertUser(user: CreateUserDTO): Future[Int] = {
    val encrypt = new EncryptString(user.password, MD5Algorithm)
    val insertTableEmail = userTable += UserRow(user.username, encrypt.result.toString)
    db.run(insertTableEmail)
  }

  /**
   * Logins an user, once this provides a matching username and password
   */
  def loginUser(user: CreateUserDTO): Future[Seq[UserRow]] = {
    val encrypt = new EncryptString(user.password, MD5Algorithm)
    val realUser = userTable.filter(x => (x.username === user.username) && x.password === encrypt.result.toString).result
    db.run(realUser)
  }

  /**
   * Inserts a login into database with the expire token session as the current server time plus 1 hour
   * @return Generated token
   */
  def insertLogin(user: CreateUserDTO): String = {
    val token = randomUUID().toString
    val active = true

    val insertTableLogin = loginTable += LoginRow(user.username, token, validate1Hour, active)

    db.run(insertTableLogin)
    token
  }

  def insertLogout(token: String): Future[Int] = {
    val notActive = false
    val insertTableLogin = loginTable.filter(_.token === token).map(_.active).update(notActive)

    db.run(insertTableLogin)

  }

  /**
   * Validates the token for 1 hour
   * @return Current server time plus 1 hour
   */
  private def validate1Hour: Long = {
    val currentTime = System.currentTimeMillis()
    val valid1Hour = currentTime + 3600000
    valid1Hour
  }
}
