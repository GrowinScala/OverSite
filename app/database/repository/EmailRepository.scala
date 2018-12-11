package database.repository

import java.util.UUID.randomUUID

import api.dto.CreateEmailDTO
import database.mappings.ChatMappings.ChatTable
import database.mappings.EmailMappings._
import database.mappings.{ BCCTable => _, CCTable => _, EmailTable => _, ToAddressTable => _, _ }
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

/**
  * Class that receives a db path
  * @param path
  * @param executionContext
  */
class EmailRepository(path: String)(implicit val executionContext: ExecutionContext) {
  /**
    * Sets a database using target path configuration
    */
  val db = Database.forConfig(path)

  /**
    * Inserts an email in the database
    * @param email
    * @return Generated email Id
    */
  def insertEmail(email: CreateEmailDTO) = {
    val randomEmailID = randomUUID().toString
    val randomChatID = randomUUID().toString
    val chatActions = new ChatRepository()
    //TODO consider replying emails(emails without a new chat id), probably with a filter query

    /**
      * val that covers all the tables related with EmailTable and inserts the information related to every email inserted
      */
    val action = for {
      _ <- chatActions.insertChat(email, randomChatID)
      _ <- EmailTable += Email(randomEmailID, email.chatID.getOrElse(randomChatID), email.fromAddress, email.dateOf, email.header, email.body, email.sendNow)
      _ <- ToAddressTable ++= email.to.getOrElse(Seq("")).map(ToAddress(randomUUID().toString, randomEmailID, _))
      _ <- CCTable ++= email.CC.getOrElse(Seq("")).map(CC(randomUUID().toString, randomEmailID, _))
      _ <- BCCTable ++= email.BCC.getOrElse(Seq("")).map(BCC(randomUUID().toString, randomEmailID, _))
    } yield randomEmailID

    /**
      * Run the action "transactionally" so all the process is aborted if an e-mail information is not capable to be
      * inserted in any of the tables
      */
    db.run(action.transactionally)
  }

}
