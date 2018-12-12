package database.repository

import java.util.UUID.randomUUID

import api.dto.CreateEmailDTO
import database.mappings.{ Chat, ChatTable }
import database.mappings.ChatMappings.ChatTable
import database.mappings.EmailMappings.{ BCCTable, CCTable, EmailTable, ToAddressTable }
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

class ChatRepository(path: String)(implicit val executionContext: ExecutionContext) {
  val db = Database.forConfig(path)

  /**
   * Insert a chat into database
   *
   * @param email
   * @param random
   * @return
   */
  def insertChat(email: CreateEmailDTO, chatID: String) = {
    val randomChatID = randomUUID().toString
    existChatID(chatID).map {
      case true => chatID
      case false =>
        db.run(ChatTable += Chat(randomChatID, email.header))
        randomChatID
    }
  }

  def existChatID(chatID: String): Future[Boolean] = {
    val tableSearch = ChatTable.filter(_.chatID === chatID).result
    db.run(tableSearch).map(_.length).map {
      case 1 => true
      case _ => false
    }
  }

  def showInbox(userName: String, token: String): Future[Vector[String]] = {
    val action = sql"""select HEADER  from emails
           left join toaddresses on (emails.EMAILID=toaddresses.EMAILID)
           left join ccs on (emails.EMAILID=ccs.EMAILID)
           left join bccs on (emails.EMAILID=bccs.EMAILID)
           where (emails.FROMADDRESS = "pluis@cmail.com"
           or toaddresses.username = "pluis@cmail.com"
           or ccs.username = "pluis@cmail.com"
           or bccs.username = "pluis@cmail.com"
           and emails.SENT=1)
           group by emails.CHATID, emails.header""".as[String]
    //EmailTable joinLeft ToAddressTable on (_.emailID === _.emailID) joinLeft CCTable on (_.emailID === _.emailID)
    //addresses joinLeft people on (_.id === _.addressId)
    /*

*/
    db.run(action)
  }
}
