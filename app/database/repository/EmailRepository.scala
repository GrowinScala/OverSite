package database.repository
import java.util.UUID.randomUUID

import api.dto.EmailCreationDTO.CreateEmailDTO
import database.mappings.EmailObject._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.Future

class EmailRepository {
  val db = Database.forConfig("mysql")

  def execDB[T](action: DBIO[T]): Future[T] = db.run(action)

  def insertEmail(email: CreateEmailDTO) = {
    val emailID = randomUUID().toString
    val insertTableEmail = EmailTable += Email(emailID, email.chatID, email.fromAddress, email.dateOf, email.header, email.body, email.sendNow)
    val insertTableToAddress = ToAddressTable ++= email.to.map(ToAddress(randomUUID().toString, emailID ,_))
    execDB(insertTableEmail)
    execDB(insertTableToAddress)

  }
}
