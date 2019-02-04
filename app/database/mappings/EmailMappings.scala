package database.mappings

import database.mappings.Destination.Destination
import definedStrings.DatabaseStrings._
import slick.jdbc.MySQLProfile.api._

/** Case class of Email Table Row */
case class EmailRow(
  emailID: String,
  chatID: String,
  fromAddress: String,
  dateOf: String,
  header: String,
  body: String,
  isTrash: Boolean)

/** Case class of DestinationEmail Table Row*/
case class DestinationEmailRow(
  emailID: String,
  username: String,
  destination: Destination,
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

  def * = (emailID, chatID, fromAddress, dateOf, header, body, isTrash) <> (EmailRow.tupled, EmailRow.unapply)
}

/** Class that defines the draft table, establishing draftID as primary key in the database*/
class DestinationEmailTable(tag: Tag) extends Table[DestinationEmailRow](tag, EmailsDestinationTable) {

  def username = column[String](UsernameRow)
  def emailID = column[String](EmailIDRow)
  def destination = column[Destination](DestinationRow)
  def isTrash = column[Boolean](TrashRow)

  def * = (emailID, username, destination, isTrash) <> (DestinationEmailRow.tupled, DestinationEmailRow.unapply)
}

/** Queries of Email table and it's nested entities: */
object EmailMappings {
  lazy val emailTable = TableQuery[EmailTable]
  lazy val destinationEmailTable = TableQuery[DestinationEmailTable]
}
