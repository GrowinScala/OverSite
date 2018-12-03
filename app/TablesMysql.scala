
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._

class Tables {

  //Case classes
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

  case class ChatUser(
                       chatUserID: String,
                       chatID: String,
                       username: String
                     )

  case class User(
                   username: String,
                   password: String)

  case class Share(
                    shareID: String,
                    chatID: String,
                    fromUser: String,
                    toID: String
                  )
  //Classes
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

  class ChatUserTable(tag:Tag) extends Table[ChatUser](tag, "chatusers") {
    def chatUserID = column[String]("chatuserID", O.PrimaryKey)
    def chatID = column[String]("chatID")
    def username= column[String]("username")
    def * = (chatUserID, chatID, username ) <> (ChatUser.tupled, ChatUser.unapply)
  }

  class UserTable(tag:Tag) extends Table[User](tag, "users") {
    def username = column[String]("username", O.PrimaryKey)
    def password = column[String]("password")
    def * = (username, password) <> (User.tupled, User.unapply)
  }

  class ShareTable(tag:Tag) extends Table[Share](tag, "shares") {
    def shareID = column[String]("shareID", O.PrimaryKey)
    def chatID = column[String]("chatID")
    def fromUser = column[String]("fromUser")
    def toID = column[String]("toID")
    def * = (shareID, chatID, fromUser, toID) <> (Share.tupled, Share.unapply)
  }
}
