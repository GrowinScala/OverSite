package database.repository

import java.util.UUID.randomUUID

import api.dto.CreateEmailDTO
import database.mappings.ChatMappings.ChatTable
import database.mappings.EmailMappings._
import database.mappings.{ BCCTable => _, CCTable => _, EmailTable => _, ToAddressTable => _, _ }
import play.api.libs.json.{ JsError, Json }
import slick.jdbc.MySQLProfile.api._
import play.api.mvc.Results._
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

    val chatActions = new ChatRepository("mysql")
    //TODO consider replying emails(emails without a new chat id), probably with a filter query

    /**
     * val that covers all the tables related with EmailTable and inserts the information related to every email inserted
     */

    val chatID = chatActions.insertChat(email, email.chatID.getOrElse(randomUUID().toString))

    val insertEmailTable = chatID.map(EmailTable += Email(randomEmailID, _, email.fromAddress, email.dateOf, email.header, email.body, email.sendNow))
    val insertAddressTable = ToAddressTable ++= email.to.getOrElse(Seq("")).map(ToAddress(randomUUID().toString, randomEmailID, _))
    val insertCCTable = CCTable ++= email.CC.getOrElse(Seq("")).map(CC(randomUUID().toString, randomEmailID, _))
    val insertBCCTable = BCCTable ++= email.BCC.getOrElse(Seq("")).map(BCC(randomUUID().toString, randomEmailID, _))

    /**
     * Run the action "transactionally" so all the process is aborted if an e-mail information is not capable to be
     * inserted in any of the tables
     */
    insertEmailTable.map(db.run(_))
    db.run(insertAddressTable)
    db.run(insertCCTable)
    db.run(insertBCCTable)
    chatID
  }

  //TODO BAD REQUEST MISSING
  def showEmails(userEmail: String, status: String) = {
    status match {
      //fromAddress
      case "sent" => {
        val querySentEmailIds = EmailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === true)
          .sortBy(_.dateOf)
          .map(x => (x.emailID, x.header)).result
        db.run(querySentEmailIds)
      }
      case "received" => {
        val queryReceivedEmailIds = ToAddressTable
          .filter(_.username === userEmail).map(_.emailID)
          .union(CCTable.filter(_.username === userEmail).map(_.emailID))
          .union(BCCTable.filter(_.username === userEmail).map(_.emailID))
        val queryReceivedEmailIdsAux = EmailTable.filter(_.emailID in queryReceivedEmailIds)
          .filter(_.sent === true)
          .sortBy(_.dateOf)
          .map(x => (x.emailID, x.header))
          .result
        db.run(queryReceivedEmailIdsAux)
      }
      case "draft" => {
        val querySentEmailIds = EmailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === false)
          .sortBy(_.dateOf)
          .map(x => (x.emailID, x.header)).result
        db.run(querySentEmailIds)
      }
      case "supervised" => {

      }
      //case _ => Future { BadRequest("****-TE") }
    }
  }
}
