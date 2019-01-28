package database.mappings

import database.mappings.Destination.Destination
import definedStrings.DatabaseStrings._
import slick.jdbc.MySQLProfile.api._

/**
 * Case class of Draft Table Row
 */
case class DraftRow(
  draftID: String,
  chatID: String,
  fromAddress: String,
  dateOf: String,
  header: String,
  body: String,
  isTrash: Boolean)

/** Case class of DestinationDraft Table Row*/
case class DestinationDraftRow(
  draftID: String,
  username: String,
  destination: Destination)

/** Class that defines the draft table, establishing draftID as primary key in the database*/
class DestinationDraftTable(tag: Tag) extends Table[DestinationDraftRow](tag, DraftsDestinationTable) {

  def draftID = column[String](DraftIDRow)
  def username = column[String](UsernameRow)
  def destination = column[Destination](DestinationRow)

  def * = (draftID, username, destination) <> (DestinationDraftRow.tupled, DestinationDraftRow.unapply)
}

/** Class that defines the draft table, establishing draftID as primary key in the database*/
class DraftTable(tag: Tag) extends Table[DraftRow](tag, DraftsTable) {

  def draftID = column[String](DraftIDRow, O.PrimaryKey)
  def chatID = column[String](ChatIDRow)
  def username = column[String](UsernameRow)
  def dateOf = column[String](DateOfRow)
  def header = column[String](HeaderRow)
  def body = column[String](BodyRow)
  def isTrash = column[Boolean](TrashRow)

  def * = (draftID, chatID, username, dateOf, header, body, isTrash) <> (DraftRow.tupled, DraftRow.unapply)

}

object DraftMappings {
  lazy val draftTable = TableQuery[DraftTable]
  lazy val destinationDraftTable = TableQuery[DestinationDraftTable]
}
