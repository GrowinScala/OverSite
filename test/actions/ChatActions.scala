package actions

import java.util.UUID.randomUUID

import api.dtos.CreateEmailDTO
import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.repository.{ ChatRepository, EmailRepository }
import javax.inject.Inject
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }
class ChatActions @Inject() (implicit val executionContext: ExecutionContext, implicit val db: Database, chatActions: ChatRepository) {

  val chatActionsTest = new ChatRepository()
  val emailActions = new EmailRepository()

  def createFilesTable = {
    db.run(emailTable.schema.create)
    db.run(chatTable.schema.create)
    db.run(toAddressTable.schema.create)
    db.run(ccTable.schema.create)
    db.run(bccTable.schema.create)
  }

  def dropFilesTable = {
    db.run(emailTable.schema.drop)
    db.run(chatTable.schema.drop)
    db.run(toAddressTable.schema.drop)
    db.run(ccTable.schema.drop)
    db.run(bccTable.schema.drop)
  }

  def deleteRowsTable = {
    db.run(emailTable.delete)
    db.run(chatTable.delete)
    db.run(toAddressTable.delete)
    db.run(ccTable.delete)
    db.run(bccTable.delete)
  }

  def insertChatTest(user: String, email: CreateEmailDTO) = {
    val chatIDAux = email.chatID.getOrElse(randomUUID().toString)
    Await.result(chatActionsTest.insertChat(email, chatIDAux), Duration("10 seconds"))
    val chatFilter = chatTable.filter(_.header === email.header).result
    Await.result(db.run(chatFilter).map(_.nonEmpty), Duration("10 seconds"))
  }

  /**
   * val emailFilter = emailTable.filter(_.dateOf === email.dateOf)
   * .filter(_.header === email.header)
   * .filter(_.body === email.body)
   * .filter(_.sent === email.sendNow).result
   *
   *     Await.result(db.run(emailFilter).map(_.nonEmpty), Duration("10 seconds"))
   */
}
