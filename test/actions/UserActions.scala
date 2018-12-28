package actions

import api.dtos.CreateUserDTO
import database.mappings.UserMappings._
import database.mappings.{ LoginRow, UserRow }
import database.repository.UserRepository
import definedStrings.AlgorithmStrings.MD5Algorithm
import encryption.EncryptString
import javax.inject.Inject
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class UserActions @Inject() (implicit val executionContext: ExecutionContext, implicit val db: Database) {

  val userActions = new UserRepository()

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

  def insertUserTest(user: CreateUserDTO) = {
    userActions.insertUser(user)

    val encrypt = new EncryptString(user.password, MD5Algorithm)

    val userFilter = userTable.filter(_.username === user.username).filter(_.password === encrypt.result.toString).result
    db.run(userFilter).map(_.nonEmpty)
  }

  def loginUserTest(user: CreateUserDTO) = {
    userActions.insertUser(user)
    val userLogin = userActions.loginUser(user)
    userLogin.map(_.nonEmpty)
  }

  def insertLoginTest(user1: CreateUserDTO, user2: CreateUserDTO) = {
    //val encrypt = new EncryptString(user.password, MD5Algorithm)
    userActions.insertUser(user1)
    userActions.insertLogin(user1)
    val loginFilter = userActions.loginUser(user2)
    loginFilter.map(_.nonEmpty)
  }

  def insertLogoutTest(user: CreateUserDTO, optionToken: Option[String], optionActive: Option[Boolean]) = {
    userActions.insertUser(user)
    val token = userActions.insertLogin(user)
    userActions.insertLogout(token)
    val logoutFilter = loginTable.filter(_.username === user.username)
      .filter(_.token === optionToken.getOrElse(token))
      .filter(_.validDate > System.currentTimeMillis())
      .filter(_.active === optionActive.getOrElse(false)).result

    db.run(logoutFilter).map(_.nonEmpty)
  }

}
