package database.mappings

import database.mappings.ChatMappings.ChatTable
import slick.jdbc.MySQLProfile.api._
import database.mappings.EmailMappings._

case class Email(
  emailID: String,
  chatID: String,
  fromAddress: String,
  dateOf: String,
  header: String,
  body: String,
  sent: Boolean)

case class ToAddress(
  toID: String,
  emailID: String,
  username: String)

case class CC(
  CCID: String,
  emailID: String,
  username: String)

case class BCC(
  BCCID: String,
  emailID: String,
  username: String)

/**
 * Class that defines the email table,establishing emailID as primary key in the database and chatId as foreign key
 * @param tag slick tag
 */
class EmailTable(tag: Tag) extends Table[Email](tag, "emails") {
  def fileIdFK = foreignKey("chatID", chatID, ChatMappings.ChatTable)(_.chatID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def chatID = column[String]("chatID")

  def emailID = column[String]("emailID", O.PrimaryKey)

  def fromAddress = column[String]("fromAddress")

  def dateOf = column[String]("dateOf")

  def header = column[String]("header")

  def body = column[String]("body")

  def sent = column[Boolean]("sent")

  def * = (emailID, chatID, fromAddress, dateOf, header, body, sent) <> (Email.tupled, Email.unapply)
}

/**
 * Class that defines the toAddress table,establishing toID as primary key in the database and emailID as foreign key
 * @param tag slick tag
 */
class ToAddressTable(tag: Tag) extends Table[ToAddress](tag, "toaddresses") {

  def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (toID, emailID, username) <> (ToAddress.tupled, ToAddress.unapply)

  def emailID = column[String]("emailID")

  def toID = column[String]("toID", O.PrimaryKey)

  def username = column[String]("username")
}

/**
 * Class that defines the cc table,establishing ccID as primary key in the database and emailID as foreign key
 * @param tag slick tag
 */
class CCTable(tag: Tag) extends Table[CC](tag, "ccs") {

  def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def emailID = column[String]("emailID")

  def * = (CCID, emailID, username) <> (CC.tupled, CC.unapply)

  def CCID = column[String]("CCID", O.PrimaryKey)

  def username = column[String]("username")
}

/**
 * Class that defines the bcc table,establishing bccID as primary key in the database and emailID as foreign key
 * @param tag slick tag
 */
class BCCTable(tag: Tag) extends Table[BCC](tag, "bccs") {

  def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def BCCID = column[String]("BCCID", O.PrimaryKey)

  def emailID = column[String]("emailID")

  def username = column[String]("username")

  def * = (BCCID, emailID, username) <> (BCC.tupled, BCC.unapply)
}

object EmailMappings {

  //Example
  /*
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
  lazy val EmailTable = TableQuery[EmailTable]
  lazy val ToAddressTable = TableQuery[ToAddressTable]
  lazy val CCTable = TableQuery[CCTable]
  lazy val BCCTable = TableQuery[BCCTable]

}
