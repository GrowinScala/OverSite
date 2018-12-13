package database.mappings
import database.mappings.ChatMappings._
import slick.jdbc.MySQLProfile.api._

/**
 * Case class of chat
 * @param chatID
 * @param header
 */
case class Chat(
  chatID: String,
  header: String)

/**
 * Case class of chatUser
 * @param chatID
 * @param header
 */
case class ChatUser(
  chatUserID: String,
  chatID: String,
  username: String)

/**
 * Case class of share
 * @param shareID
 * @param chatID
 * @param fromUser
 * @param toID
 */
case class Share(
  shareID: String,
  chatID: String,
  fromUser: String,
  toID: String)

/**
 * Class that defines the chat table, establishing chatID as primary key in the database
 * @param tag
 */
class ChatTable(tag: Tag) extends Table[Chat](tag, "chats") {

  def chatID = column[String]("chatID", O.PrimaryKey)
  def header = column[String]("header")

  def * = (chatID, header) <> (Chat.tupled, Chat.unapply)
}

/**
 * Class that defines the chatUser table, establishing chatUserID as primary key in the database,
 * chatID and username as foreign keys
 * @param tag
 */
class ChatUserTable(tag: Tag) extends Table[ChatUser](tag, "chatusers") {
  def chatUserID = column[String]("chatuserID", O.PrimaryKey)
  def chatID = column[String]("chatID")
  def username = column[String]("username")
  def fileIdFK1 = foreignKey("chatID", chatID, ChatTable)(_.chatID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def fileIdFK2 = foreignKey("username", username, UserMappings.UserTable)(_.username, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (chatUserID, chatID, username) <> (ChatUser.tupled, ChatUser.unapply)
}

/**
 * Class that defines the share table, establishing shareID as primary key in the database
 * and chatID as foreign key
 * @param tag
 */
class ShareTable(tag: Tag) extends Table[Share](tag, "shares") {
  def shareID = column[String]("shareID", O.PrimaryKey)
  def chatID = column[String]("chatID")
  def fromUser = column[String]("fromUser")
  def toID = column[String]("toID")
  def fileIdFK = foreignKey("chatID", chatID, ChatTable)(_.chatID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (shareID, chatID, fromUser, toID) <> (Share.tupled, Share.unapply)
}

/**
 * ???
 */
object ChatMappings {
  //Example
  /*
  implicit class QueryExtensions(q: Query[ChatTable, Chat, Seq]) {
    def filterByChatID(chatID: Option[String]): Query[ChatTable, Chat, Seq] =
      ChatTable.filter(_.chatID === chatID)
  }
 implicit class QueryExtensions(q: Query[JourneysTable, JourneysRow, Seq]) {
     def byJourneyId(journeyId: JourneyId): Query[JourneysTable, JourneysRow, Seq] =
      q.filter(_.journeyId === journeyId)


  def byJourneyName(journeyName: String): Query[JourneysTable, JourneysRow, Seq] =
    q.filter(_.journeyName === journeyName)

  def byProfileGUId(profileGUId: ProfileGUId): Query[JourneysTable, JourneysRow, Seq] =
    q.filter(_.profileGUId === profileGUId)

  def archived(boolOpt: Option[Boolean]): Query[JourneysTable, JourneysRow, Seq] =
    boolOpt.map(bool => q.filter(_.journeyArchived === Indicator(bool))).getOrElse(q)

  def archived: Query[JourneysTable, JourneysRow, Seq] = q.filter(_.journeyArchived === Indicator.True)

  def notArchived: Query[JourneysTable, JourneysRow, Seq] = q.filter(_.journeyArchived === Indicator.False)
}

lazy val all = TableQuery[JourneysTable]
 */

  /**
   * Queries of user table and login table
   */
  lazy val ChatTable = TableQuery[ChatTable]
  lazy val chatUserTable = TableQuery[ChatUserTable]
  lazy val ShareTable = TableQuery[ShareTable]
}
