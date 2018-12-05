package database.repository
import java.util.UUID.randomUUID

import api.dto.EmailCreationDTO.CreateEmailDTO
import database.mappings.EmailObject._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._


class EmailRepository {
  val db = Database.forConfig("mysql")

  //TODO put into asinc using future
  def execDB[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  def insertEmail(email: CreateEmailDTO): Int = {
    val queryAction = EmailTable += Email(randomUUID().toString,email.chatID,email.fromAddress,email.dateOf,email.header,email.body)
    execDB(queryAction)
  }
}
