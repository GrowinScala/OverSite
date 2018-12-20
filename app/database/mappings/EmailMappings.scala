package database.mappings

import database.mappings.EmailMappings._
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
  sent: Boolean)

/**
 * Case class of ToAddress Table Row
 */
case class ToAddressRow(
  toID: String,
  emailID: String,
  username: String)

/**
 * Case class of CC Table Row
 */
case class CCRow(
  CCID: String,
  emailID: String,
  username: String)

/**
 * Case class of BCC Table Row
 */
case class BCCRow(
  BCCID: String,
  emailID: String,
  username: String)

/**
 * Class that defines the email table,establishing emailID as primary key in the database and chatId as foreign key
 * @param tag
 */
class EmailTable(tag: Tag) extends Table[EmailRow](tag, "emails") {
  def fileIdFK = foreignKey("chatID", chatID, ChatMappings.chatTable)(_.chatID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def chatID = column[String]("chatID")
  def emailID = column[String]("emailID", O.PrimaryKey)
  def fromAddress = column[String]("fromAddress")
  def dateOf = column[String]("dateOf")
  def header = column[String]("header")
  def body = column[String]("body")
  def sent = column[Boolean]("sent")

  def * = (emailID, chatID, fromAddress, dateOf, header, body, sent) <> (EmailRow.tupled, EmailRow.unapply)
}

/**
 * Class that defines the toAddress table,establishing toID as primary key in the database and emailID as foreign key
 * @param tag
 */
class ToAddressTable(tag: Tag) extends Table[ToAddressRow](tag, "toaddresses") {
  def fileIdFK = foreignKey("emailID", emailID, emailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def emailID = column[String]("emailID")
  def toID = column[String]("toID", O.PrimaryKey)
  def username = column[String]("username")

  def * = (toID, emailID, username) <> (ToAddressRow.tupled, ToAddressRow.unapply)
}

/**
 * Class that defines the cc table,establishing ccID as primary key in the database and emailID as foreign key
 * @param tag
 */
class CCTable(tag: Tag) extends Table[CCRow](tag, "ccs") {
  def fileIdFK = foreignKey("emailID", emailID, emailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def emailID = column[String]("emailID")
  def CCID = column[String]("CCID", O.PrimaryKey)
  def username = column[String]("username")

  def * = (CCID, emailID, username) <> (CCRow.tupled, CCRow.unapply)
}

/**
 * Class that defines the bcc table,establishing bccID as primary key in the database and emailID as foreign key
 * @param tag
 */
class BCCTable(tag: Tag) extends Table[BCCRow](tag, "bccs") {
  def fileIdFK = foreignKey("emailID", emailID, emailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def BCCID = column[String]("BCCID", O.PrimaryKey)
  def emailID = column[String]("emailID")
  def username = column[String]("username")

  def * = (BCCID, emailID, username) <> (BCCRow.tupled, BCCRow.unapply)
}

object EmailMappings {
  /**
   * Queries of Email table and it's nested entities:
   */
  lazy val emailTable = TableQuery[EmailTable]
  lazy val toAddressTable = TableQuery[ToAddressTable]
  lazy val ccTable = TableQuery[CCTable]
  lazy val bccTable = TableQuery[BCCTable]
}
