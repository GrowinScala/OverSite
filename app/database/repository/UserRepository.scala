package database.repository

import java.util.UUID.randomUUID

import api.dtos.CreateUserDTO
import com.google.inject.AbstractModule
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import encryption.EncryptString
import com.google.inject.Inject
import play.api.inject.Module
import play.inject.Module
import slick.basic.DatabaseConfig
import slick.jdbc.MySQLProfile
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }
/**
 * Class that receives a db path
 */
//TODO: Reimplement using Trait + Implementation Class instead. Will make Injection and BL/DL separation easier which you currently are tangling a bit.
//Also you don't need to use Injection here.
class UserRepository @Inject() (db: Database)(implicit val executionContext: ExecutionContext) {

  /**
   * Insert an user into database with is password encrypted
   * @return The number of insertions into database
   */
  def insertUser(user: CreateUserDTO): Future[Int] = {
    val encrypt = new EncryptString(user.password, "MD5")
    val insertTableEmail = UserTable += UserRow(user.username, encrypt.result.toString)
    db.run(insertTableEmail)
  }

  /**
   * Logins an user, once this provides a matching username and password
   */
  def loginUser(user: CreateUserDTO): Future[Seq[UserRow]] = {
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
    val active = true

    val insertTableLogin = LoginTable += LoginRow(user.username, token, validate1Hour, active)

    db.run(insertTableLogin)
    token
  }

  def insertLogout(token: String) = {
    val notActive = false
    val insertTableLogin = LoginTable.filter(_.token === token).map(_.active).update(notActive)

    db.run(insertTableLogin)

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
  def validateToken(userName: String, token: String): Future[Seq[LoginRow]] = {
    val validateTableToken = LoginTable.filter(x => (x.username === userName) && (x.token === token) && x.validDate > System.currentTimeMillis()).result
    db.run(validateTableToken)
  }
}
