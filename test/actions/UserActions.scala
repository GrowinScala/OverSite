package actions

import api.dtos.CreateUserDTO
import database.mappings.UserMappings._
import database.repository.UserRepository
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
    val userFilter = userTable.filter(_.username === user.username).filter(_.password === user.password).result
    db.run(userFilter).map(_.nonEmpty)
  }

}
