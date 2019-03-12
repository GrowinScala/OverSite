package database.repository

import java.util.UUID.randomUUID

import api.dtos.CreateUserDTO
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import database.properties.DBProperties
import definedStrings.AlgorithmStrings._
import encryption.EncryptString
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }
/**  Class that receives a db path */
class UserRepositoryImpl @Inject() (dbClass: DBProperties)(implicit val executionContext: ExecutionContext, implicit val tokenValidationTime: Long) extends UserRepository {
  val db = dbClass.db

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
      .filter(_.password === encrypt.result.toString)
      .result

    db.run(realUser).map(users =>
      users.map(userRow =>
        CreateUserDTO(userRow.username, userRow.password)))
  }

  /**
   * Inserts a login into database with the expire token session as the current server time plus 1 hour
   * @return Generated token
   */
  def insertLogin(user: CreateUserDTO): Future[String] = {

    /**
     * Validates the token for N hours depending on config
     * @return Current server time plus N hours
     */
    def validateNHours: Long = {
      val currentTime = System.currentTimeMillis()
      val valid2Hours = currentTime + tokenValidationTime
      valid2Hours
    }

    val token = randomUUID().toString

    for {
      _ <- db.run(loginTable += LoginRow(user.username, token, validateNHours, active = true))
    } yield token

  }

  /** Patches the ACTIVE column to false */
  def insertLogout(token: String): Future[Int] = {

    val insertTableLogin = loginTable
      .filter(_.token === token)
      .map(_.active)
      .update(false)

    db.run(insertTableLogin)

  }

}
