package database.repository

import java.util.UUID.randomUUID

import api.dto.CreateEmailDTO
import database.mappings.EmailMappings._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

class EmailRepository(path: String)(implicit val executionContext: ExecutionContext) {
  val db = Database.forConfig(path)

  def execDB[T](action: DBIO[T]): Future[T] = db.run(action)

  def insertEmail(email: CreateEmailDTO) = {
    val randomEmailID = randomUUID().toString
    val randomChatID = randomUUID().toString
    val action = for {
      _ <- ChatsTable += Chats(email.chatID.getOrElse(randomChatID), email.header)
      _ <- EmailTable += Email(randomEmailID, email.chatID.getOrElse(randomChatID), email.fromAddress, email.dateOf, email.header, email.body, email.sendNow)
      _ <- ToAddressTable ++= email.to.getOrElse(Seq("")).map(ToAddress(randomUUID().toString, randomEmailID, _))
      _ <- CCTable ++= email.CC.getOrElse(Seq("")).map(CC(randomUUID().toString, randomEmailID, _))
      _ <- BCCTable ++= email.BCC.getOrElse(Seq("")).map(BCC(randomUUID().toString, randomEmailID, _))
    } yield randomEmailID
    db.run(action.transactionally)
  }

}
