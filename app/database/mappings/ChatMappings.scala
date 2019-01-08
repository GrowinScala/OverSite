package database.mappings
import database.mappings.ChatMappings._
import slick.jdbc.MySQLProfile.api._
import definedStrings.DatabaseStrings._

/** Case class of Chat Table Row: */
case class ChatRow(
  chatID: String,
  header: String)

/** Case class of Chat User Table Row: */
case class ChatUserRow(
  chatUserID: String,
  chatID: String,
  username: String)

/** Case class of Share Table Row: */
case class ShareRow(
  shareID: String,
  chatID: String,
  fromUser: String,
  toUser: String)

/** Class that defines the chat table, establishing chatID as primary key in the database */
class ChatTable(tag: Tag) extends Table[ChatRow](tag, ChatsTable) {

  def chatID = column[String](ChatIDRow, O.PrimaryKey)
  def header = column[String](HeaderRow)

  def * = (chatID, header) <> (ChatRow.tupled, ChatRow.unapply)
}

/**
 * Class that defines the share table, establishing shareID as primary key in the database
 * and chatID as foreign key
 */
class ShareTable(tag: Tag) extends Table[ShareRow](tag, SharesTable) {
  def shareID = column[String](ShareIDRow, O.PrimaryKey)
  def chatID = column[String](ChatIDRow)
  def fromUser = column[String](FromUserRow)
  def toID = column[String](ToIDRow)
  //def fileIdFK = foreignKey(ChatIDRow, chatID, chatTable)(_.chatID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (shareID, chatID, fromUser, toID) <> (ShareRow.tupled, ShareRow.unapply)
}

object ChatMappings {

  /** Queries of user table and login table */
  lazy val chatTable = TableQuery[ChatTable]
  lazy val shareTable = TableQuery[ShareTable]
}
