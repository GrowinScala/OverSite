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
/**  Class that receives a db path */
class UserRepositoryImpl @Inject() (implicit val executionContext: ExecutionContext, db: Database) extends UserRepository {

  /**
   * Insert an user into database with is password encrypted
   * @return The number of insertions into database
   */
  def insertUser(user: CreateUserDTO): Future[Int] = {
    val encrypt = new EncryptString(user.password, MD5Algorithm)
    val insertTableEmail = userTable += UserRow(user.username, encrypt.result.toString)
    db.run(insertTableEmail)
  }

  /** Logins an user, once this provides a matching username and password */
  def loginUser(user: CreateUserDTO): Future[Seq[CreateUserDTO]] = {
    val encrypt = new EncryptString(user.password, MD5Algorithm)
    val realUser = userTable.filter(_.username === user.username)
      .filter(_.password === encrypt.result.toString).result
    db.run(realUser).map(users =>
      users.map(userRow =>
        CreateUserDTO(userRow.username, userRow.password)))
  }

  /**
   * Inserts a login into database with the expire token session as the current server time plus 1 hour
   * @return Generated token
   */
  def insertLogin(user: CreateUserDTO): Future[String] = {
    val token = randomUUID().toString
    val active = true

    for {
      _ <- db.run(loginTable += LoginRow(user.username, token, validate1Hour, active))
    } yield token

  }

  /** Patches the ACTIVE column to false */
  def insertLogout(token: String): Future[Int] = {
    val notActive = false
    val insertTableLogin = loginTable
      .filter(_.token === token)
      .map(_.active)
      .update(notActive)

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
