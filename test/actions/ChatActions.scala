package actions

import java.util.UUID.randomUUID

import api.dtos.CreateEmailDTO
import database.mappings.ChatMappings._
import database.repository.{ ChatRepository, ChatRepositoryImpl, EmailRepositoryImpl }
import javax.inject.Inject
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }
class ChatActions @Inject() (implicit val executionContext: ExecutionContext, implicit val db: Database, chatActions: ChatRepositoryImpl) {

  val chatActionsTest = new ChatRepositoryImpl()
  val emailActions = new EmailRepositoryImpl()

  def insertChatTest(user: String, email: CreateEmailDTO) = {
    val chatIDAux = email.chatID.getOrElse(randomUUID().toString)
    Await.result(chatActionsTest.insertChat(email, chatIDAux), Duration("10 seconds"))
    val chatFilter = chatTable.filter(_.header === email.header).result
    Await.result(db.run(chatFilter).map(_.nonEmpty), Duration("10 seconds"))
  }

}
