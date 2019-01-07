package actions

import api.dtos.CreateUserDTO
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import database.repository.UserRepositoryImpl
import definedStrings.AlgorithmStrings.MD5Algorithm
import encryption.EncryptString
import javax.inject.Inject
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class UserActions @Inject() (implicit val executionContext: ExecutionContext, implicit val db: Database) extends SupportActions {

  val userActions = new UserRepositoryImpl()

  def createFilesTable = {
    db.run(loginTable.schema.create)
    db.run(userTable.schema.create)
  }

  def dropFilesTable = {
    db.run(loginTable.schema.drop)
    db.run(userTable.schema.drop)
  }

  def deleteRowsTable = {
    db.run(loginTable.delete)
    db.run(userTable.delete)
  }
  /*
  def insertUserTest(user: CreateUserDTO) = {
    waitToComplete(userActions.insertUser(user))

    val encrypt = new EncryptString(user.password, MD5Algorithm)

    val userFilter = userTable.filter(_.username === user.username).filter(_.password === encrypt.result.toString).result
    waitToComplete(db.run(userFilter).map(_.nonEmpty))
  }

  def loginUserTest(user: CreateUserDTO) = {
    waitToComplete(userActions.insertUser(user))
    val userLogin = waitToComplete(userActions.loginUser(user))
    userLogin.nonEmpty
  }

  def insertLoginTest(user1: CreateUserDTO, user2: CreateUserDTO) = {
    //val encrypt = new EncryptString(user.password, MD5Algorithm)
    waitToComplete(userActions.insertUser(user1))
    userActions.insertLogin(user1)
    val loginFilter = waitToComplete(userActions.loginUser(user2))
    loginFilter.nonEmpty
  }

  def insertLogoutTest(user: CreateUserDTO, optionToken: Option[String], optionActive: Option[Boolean]) = {
    waitToComplete(userActions.insertUser(user))
    val token = userActions.insertLogin(user)
    waitToComplete(userActions.insertLogout(token))
    val logoutFilter = loginTable.filter(_.username === user.username)
      .filter(_.validDate > System.currentTimeMillis())
      .filter(_.active === optionActive.getOrElse(false)).result

    waitToComplete(db.run(logoutFilter).map(_.nonEmpty))
  }
*/
}
