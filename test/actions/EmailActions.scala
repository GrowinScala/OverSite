package actions

import api.dtos.{CreateEmailDTO, CreateUserDTO}
import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.repository.{ChatRepository, EmailRepository, UserRepository}
import javax.inject.Inject
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class EmailActions @Inject() (implicit val executionContext: ExecutionContext, implicit val db: Database, chatActions: ChatRepository) {

  val userActions = new UserRepository()
  val emailActions = new EmailRepository()

  def createFilesTable = {
    db.run(emailTable.schema.create)
    db.run(chatTable.schema.create)
    db.run(toAddressTable.schema.create)
    db.run(ccTable.schema.create)
    db.run(bccTable.schema.create)
    //db.run(loginTable.schema.create)
    //db.run(userTable.schema.create)
  }

  def dropFilesTable = {
    db.run(emailTable.schema.drop)
    db.run(chatTable.schema.drop)
    db.run(toAddressTable.schema.drop)
    db.run(ccTable.schema.drop)
    db.run(bccTable.schema.drop)
    //db.run(loginTable.schema.drop)
    //db.run(userTable.schema.drop)
  }

  def deleteRowsTable = {
    db.run(emailTable.delete)
    db.run(chatTable.delete)
    db.run(toAddressTable.delete)
    db.run(ccTable.delete)
    db.run(bccTable.delete)
    //db.run(loginTable.delete)
    //db.run(userTable.delete)
  }

  def insertEmailTest(user: CreateUserDTO, email: CreateEmailDTO) = {
    //userActions.insertUser(user)
    //userActions.insertLogin(user)
    emailActions.insertEmail(user.username, email)
    //Filtering with the non optional parameters of an email
    val emailFilter = emailTable.result
    db.run(emailFilter).map(_.nonEmpty)
  }

}
