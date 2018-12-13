package database.repository

import java.util.UUID.randomUUID

import api.dto.{CreateChatDTO, CreateEmailDTO}
import database.mappings.{Chat, ChatTable}
import database.mappings.ChatMappings.ChatTable
import database.mappings.EmailMappings.{BCCTable, CCTable, EmailTable, ToAddressTable}
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

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


  def showInbox(userName: String, token: String): Future[Vector[(String, String)]] = {
    val action = sql"""select CHATID, HEADER  from emails
           left join toaddresses on (emails.EMAILID=toaddresses.EMAILID)
           left join ccs on (emails.EMAILID=ccs.EMAILID)
           left join bccs on (emails.EMAILID=bccs.EMAILID)
           where (emails.FROMADDRESS = "pluis@hotmail.com"
           or toaddresses.username = "pluis@hotmail.com"
           or ccs.username = "pluis@hotmail.com"
           or bccs.username = "pluis@hotmail.com"
           and emails.SENT=1)
           group by emails.CHATID, emails.header""".as[(String, String)]
    //EmailTable joinLeft ToAddressTable on (_.emailID === _.emailID) joinLeft CCTable on (_.emailID === _.emailID)
    //addresses joinLeft people on (_.id === _.addressId)

    db.run(action)
  }

  def newStuff: Unit = {
    val userEmail = "pluis@hotmail.com"
    val queryResult = for {
      emailIds: Query[Rep[String], String, Seq] <- EmailTable.filter(_.fromAddress === userEmail).map(_.emailID) union
        ToAddressTable.filter(_.username === userEmail).map(_.emailID) union
        CCTable.filter(_.username === userEmail).map(_.emailID) union
        BCCTable.filter(_.username === userEmail).map(_.emailID)
      res: Seq[(String, String)] <- EmailTable.filter(_.emailID in emailIds).map(x => (x.chatID, x.header)).result
      entry: (String, String) <- res
    } yield {
      val result = entry match {
        case (chatId, header) => CreateChatDTO(chatId, header)
      }
      result
    }

    println()
  }


    /*
           SELECT chatid, header  FROM emails e
           LEFT JOIN toaddresses to
             on (e.emailid=to.emailid)
           LEFT JOIN ccs c
             on (e.emailid=c.emailid)
           LEFT JOIN bccs b
             on (e.emailid=b.emailid)
           WHERE (e.fromaddress = "pluis@hotmail.com"
             or to.username = "pluis@hotmail.com"
             or c.username = "pluis@hotmail.com"
             or b.username = "pluis@hotmail.com")
           and emails.SENT=1
           GROUP BY e.chatid, e.header
     */
}
