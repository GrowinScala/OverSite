
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._

class TablesMysql {

  //------------------------------------------Case Classes------------------------------------------//

  case class Email(
    emailID : String,
    chatID  : String,
    fromAdress : String,
    dateOf : String,
    header  : String,
    body : String
    )

  case class Chat(
    chatID : String,
    header : String
    )

  case class ToAdress(
     toID : String,
     emailID : String,
     username : String
     )

  case class CC(
     CCID : String,
     emailID : String,
     username : String
     )
  case class BCC(
     BCCID : String,
     emailID : String,
     username : String
     )

  //------------------------------------------Table Classes------------------------------------------//

  class ChatTable(tag: Tag) extends Table[Chat](tag, "chats"){

    def chatID = column[String]("chatID", O.PrimaryKey)
    def header = column[String]("header")

    def * = ( chatID,header) <> (Chat.tupled, Chat.unapply)
  }

  class EmailTable(tag: Tag) extends Table[Email](tag, "emails") {

    def emailID = column[String]("emailID", O.PrimaryKey)
    def chatID  = column[String]("chatID")
    def fromAdress = column[String]("fromAdress")
    def dateOf  = column[String]("dateOf")
    def header = column[String]("header")
    def body  = column[String]("body")

    def fileIdFK = foreignKey("chatID", chatID, TableQuery[ChatTable])(_.chatID, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (emailID, chatID, fromAdress,dateOf,header,body) <> (Email.tupled, Email.unapply)
  }

  class ToAdressTable(tag: Tag) extends Table[ToAdress](tag,"toadresses"){

    def toID = column[String]("toID", O.PrimaryKey)
    def emailID = column[String]("emailID")
    def username = column[String]("username")

    def fileIdFK = foreignKey("emailID", emailID, TableQuery[EmailTable])(_.emailID, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (toID,emailID,username) <> (ToAdress.tupled, ToAdress.unapply)

  }

  class CCTable(tag: Tag) extends Table[ToAdress](tag,"ccs"){

    def CCID = column[String]("CCID", O.PrimaryKey)
    def emailID = column[String]("emailID")
    def username = column[String]("username")

    def fileIdFK = foreignKey("emailID", emailID, TableQuery[EmailTable])(_.emailID, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (CCID,emailID,username) <> (ToAdress.tupled, ToAdress.unapply)

  }

  class BCCTable(tag: Tag) extends Table[ToAdress](tag,"bccs"){

    def BCCID = column[String]("BCCID", O.PrimaryKey)
    def emailID = column[String]("emailID")
    def username = column[String]("username")

    def fileIdFK = foreignKey("emailID", emailID, TableQuery[EmailTable])(_.emailID, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (BCCID,emailID,username) <> (ToAdress.tupled, ToAdress.unapply)

  }

}
