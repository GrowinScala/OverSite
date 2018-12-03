
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._

class Tables {

  case class Email(
    emailID : String,
    chatID  : String,
    fromAdress : String,
    dateOf : String,
    header  : String,
    body : String
    )

  case class Chat(
    chatID : String,
    header : String
    )

  class ChatTable(tag: Tag) extends Table[Chat](tag, "chats"){

    def chatID = column[String]("chatID", O.PrimaryKey)
    def header = column[String]("header")

    def * = ( chatID,header) <> (Chat.tupled, Chat.unapply)
  }

  class EmailTable(tag: Tag) extends Table[Email](tag, "emails") {

    def emailID = column[String]("emailID", O.PrimaryKey)
    def chatID  = column[String]("chatID")
    def fromAdress = column[String]("fromAdress")
    def dateOf  = column[String]("dateOf")
    def header = column[String]("header")
    def body  = column[String]("body")

    def fileIdFK = foreignKey("chatID", chatID, TableQuery[ChatTable])(_.chatID, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (emailID, chatID, fromAdress,dateOf,header,body) <> (Email.tupled, Email.unapply)
  }

}
