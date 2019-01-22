package database.mappings

import database.mappings.Destination.Destination
import definedStrings.DatabaseStrings._
import slick.jdbc.MySQLProfile.api._

/**
 * Case class of Email Table Row
 */
case class EmailRow(
  emailID: String,
  chatID: String,
  fromAddress: String,
  dateOf: String,
  header: String,
  body: String,
  isTrash: Boolean)

/**
 * Case class of ToAddress Table Row
 */
case class ToAddressRow(
  toID: String,
  emailID: String,
  username: String,
  isTrash: Boolean)

/**
 * Case class of CC Table Row
 */
case class CCRow(
  CCID: String,
  emailID: String,
  username: String,
  isTrash: Boolean)

/**
 * Case class of BCC Table Row
 */
case class BCCRow(
  BCCID: String,
  emailID: String,
  username: String,
  isTrash: Boolean)

/** Class that defines the email table,establishing emailID as primary key in the database and chatId as foreign key */
class EmailTable(tag: Tag) extends Table[EmailRow](tag, EmailsTable) {
  def chatID = column[String](ChatIDRow)
  def emailID = column[String](EmailIDRow, O.PrimaryKey)
  def fromAddress = column[String](FromAddressRow)
  def dateOf = column[String](DateOfRow)
  def header = column[String](HeaderRow)
  def body = column[String](BodyRow)
  def isTrash = column[Boolean](TrashRow)

  //def fileIdFK = foreignKey(ChatIDRow, chatID, ChatMappings.chatTable)(_.chatID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (emailID, chatID, fromAddress, dateOf, header, body, isTrash) <> (EmailRow.tupled, EmailRow.unapply)
}

/** Class that defines the toAddress table,establishing toID as primary key in the database and emailID as foreign key */
class ToAddressTable(tag: Tag) extends Table[ToAddressRow](tag, ToAddressesTable) {
  //def fileIdFK = foreignKey(EmailIDRow, emailID, emailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def emailID = column[String](EmailIDRow)
  def toID = column[String](ToIDRow, O.PrimaryKey)
  def username = column[String](UsernameRow)
  def isTrash = column[Boolean](TrashRow)
  def * = (toID, emailID, username, isTrash) <> (ToAddressRow.tupled, ToAddressRow.unapply)
}

/** Class that defines the cc table,establishing ccID as primary key in the database and emailID as foreign key*/
class CCTable(tag: Tag) extends Table[CCRow](tag, CCsTable) {
  //def fileIdFK = foreignKey(EmailIDRow, emailID, emailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def emailID = column[String](EmailIDRow)
  def CCID = column[String](CCIDRow, O.PrimaryKey)
  def username = column[String](UsernameRow)
  def isTrash = column[Boolean](TrashRow)

  def * = (CCID, emailID, username, isTrash) <> (CCRow.tupled, CCRow.unapply)
}

/** Class that defines the bcc table,establishing bccID as primary key in the database and emailID as foreign key*/
class BCCTable(tag: Tag) extends Table[BCCRow](tag, BCCsTable) {
  //def fileIdFK = foreignKey(EmailIDRow, emailID, emailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def BCCID = column[String](BCCIDRow, O.PrimaryKey)
  def emailID = column[String](EmailIDRow)
  def username = column[String](UsernameRow)
  def isTrash = column[Boolean](TrashRow)

  def * = (BCCID, emailID, username, isTrash) <> (BCCRow.tupled, BCCRow.unapply)
}

object EmailMappings {
  /** Queries of Email table and it's nested entities: */
  lazy val emailTable = TableQuery[EmailTable]
  lazy val toAddressTable = TableQuery[ToAddressTable]
  lazy val ccTable = TableQuery[CCTable]
  lazy val bccTable = TableQuery[BCCTable]
}
