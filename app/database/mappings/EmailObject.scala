package database.mappings
import java.util.UUID.randomUUID

import api.dto.EmailCreationDTO.CreateEmailDTO
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._

object EmailObject {

  case class Email(
    emailID:     String,
    chatID:      String,
    fromAddress: String,
    dateOf:      String,
    header:      String,
    body:        String
  )

  case class ToAddress(
    toID:     String,
    emailID:  String,
    username: String
  )

  case class CC(
    CCID:     String,
    emailID:  String,
    username: String
  )
  case class BCC(
    BCCID:    String,
    emailID:  String,
    username: String
  )

  class EmailTable(tag: Tag) extends Table[Email](tag, "emails") {

    def emailID = column[String]("emailID", O.PrimaryKey)
    def chatID = column[String]("chatID")
    def fromAddress = column[String]("fromAddress")
    def dateOf = column[String]("dateOf")
    def header = column[String]("header")
    def body = column[String]("body")

    def fileIdFK = foreignKey("chatID", chatID, ChatObject.ChatTable)(_.chatID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (emailID, chatID, fromAddress, dateOf, header, body) <> (Email.tupled, Email.unapply)
  }

  class ToAddressTable(tag: Tag) extends Table[ToAddress](tag, "toaddresses") {

    def toID = column[String]("toID", O.PrimaryKey)

    def emailID = column[String]("emailID")

    def username = column[String]("username")

    def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (toID, emailID, username) <> (ToAddress.tupled, ToAddress.unapply)
  }
  class CCTable(tag: Tag) extends Table[CC](tag, "ccs") {

    def CCID = column[String]("CCID", O.PrimaryKey)

    def emailID = column[String]("emailID")

    def username = column[String]("username")

    def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (CCID, emailID, username) <> (CC.tupled, CC.unapply)
  }
  class BCCTable(tag: Tag) extends Table[BCC](tag, "bccs") {

    def BCCID = column[String]("BCCID", O.PrimaryKey)

    def emailID = column[String]("emailID")

    def username = column[String]("username")

    def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (BCCID, emailID, username) <> (BCC.tupled, BCC.unapply)
  }

  lazy val EmailTable = TableQuery[EmailTable]

  lazy val ToAddressTable = TableQuery[ToAddressTable]

  lazy val CCTable = TableQuery[CCTable]

  lazy val BCCTable = TableQuery[BCCTable]

}
