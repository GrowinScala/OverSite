package database.mappings
import slick.jdbc.MySQLProfile.api._

object ChatObject {

  case class Chat(
    chatID: String,
    header: String)

  case class ChatUser(
    chatUserID: String,
    chatID: String,
    username: String)

  case class Share(
    shareID: String,
    chatID: String,
    fromUser: String,
    toID: String)

  class ChatTable(tag: Tag) extends Table[Chat](tag, "chats") {

    def chatID = column[String]("chatID", O.PrimaryKey)
    def header = column[String]("header")

    def * = (chatID, header) <> (Chat.tupled, Chat.unapply)
  }

  class ChatUserTable(tag: Tag) extends Table[ChatUser](tag, "chatusers") {
    def chatUserID = column[String]("chatuserID", O.PrimaryKey)
    def chatID = column[String]("chatID")
    def username = column[String]("username")
    def * = (chatUserID, chatID, username) <> (ChatUser.tupled, ChatUser.unapply)
  }

  class ShareTable(tag: Tag) extends Table[Share](tag, "shares") {
    def shareID = column[String]("shareID", O.PrimaryKey)
    def chatID = column[String]("chatID")
    def fromUser = column[String]("fromUser")
    def toID = column[String]("toID")
    def * = (shareID, chatID, fromUser, toID) <> (Share.tupled, Share.unapply)
  }

  lazy val ChatTable = TableQuery[ChatTable]
  lazy val chatUserTable = TableQuery[ChatUserTable]
  lazy val ShareTable = TableQuery[ShareTable]

}
