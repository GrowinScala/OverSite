package database.mappings

import slick.jdbc.MySQLProfile.api._

object EmailMappings {

  case class Email(
                    emailID: String,
                    chatID: String,
                    fromAddress: String,
                    dateOf: String,
                    header: String,
                    body: String,
                    sent: Boolean)

  case class Chats(
                    chatID: String,
                    header: String,
                  )

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

  class ChatsTable(tag: Tag) extends Table[Chats](tag, "chats") {

    def chatID = column[String]("chatID", O.PrimaryKey)

    def header = column[String]("header")

    def * = (chatID, header) <> (Chats.tupled, Chats.unapply)
  }

  class ToAddressTable(tag: Tag) extends Table[ToAddress](tag, "toaddresses") {

    def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (toID, emailID, username) <> (ToAddress.tupled, ToAddress.unapply)

    def emailID = column[String]("emailID")

    def toID = column[String]("toID", O.PrimaryKey)

    def username = column[String]("username")
  }

  class CCTable(tag: Tag) extends Table[CC](tag, "ccs") {

    def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def emailID = column[String]("emailID")

    def * = (CCID, emailID, username) <> (CC.tupled, CC.unapply)

    def CCID = column[String]("CCID", O.PrimaryKey)

    def username = column[String]("username")
  }

  class BCCTable(tag: Tag) extends Table[BCC](tag, "bccs") {

    def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def BCCID = column[String]("BCCID", O.PrimaryKey)

    def emailID = column[String]("emailID")

    def username = column[String]("username")

    def * = (BCCID, emailID, username) <> (BCC.tupled, BCC.unapply)
  }

  lazy val EmailTable = TableQuery[EmailTable]
  lazy val ChatsTable = TableQuery[ChatsTable]
  lazy val ToAddressTable = TableQuery[ToAddressTable]
  lazy val CCTable = TableQuery[CCTable]
  lazy val BCCTable = TableQuery[BCCTable]

}
