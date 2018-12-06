package database.mappings
import slick.jdbc.MySQLProfile.api._

object ChatMappings {

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
  //def foreign key
  class ChatUserTable(tag: Tag) extends Table[ChatUser](tag, "chatusers") {
    def chatUserID = column[String]("chatuserID", O.PrimaryKey)
    def chatID = column[String]("chatID")
    def username = column[String]("username")
    def fileIdFK1 = foreignKey("chatID", chatID, ChatTable)(_.chatID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
    def fileIdFK2 = foreignKey("username", username, UserMappings.UserTable)(_.username, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (chatUserID, chatID, username) <> (ChatUser.tupled, ChatUser.unapply)
  }
  //def foreign key
  class ShareTable(tag: Tag) extends Table[Share](tag, "shares") {
    def shareID = column[String]("shareID", O.PrimaryKey)
    def chatID = column[String]("chatID")
    def fromUser = column[String]("fromUser")
    def toID = column[String]("toID")
    def fileIdFK = foreignKey("chatID", chatID, ChatTable)(_.chatID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (shareID, chatID, fromUser, toID) <> (Share.tupled, Share.unapply)
  }

  lazy val ChatTable = TableQuery[ChatTable]
  lazy val chatUserTable = TableQuery[ChatUserTable]
  lazy val ShareTable = TableQuery[ShareTable]

}
