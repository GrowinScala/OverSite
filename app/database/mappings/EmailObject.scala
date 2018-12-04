package database.mappings
import slick.jdbc.MySQLProfile.api._
import database.mappings.ChatObject._

import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import database.mappings.ChatObject._
import database.mappings.EmailObject._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await

object EmailObject {

  case class Email(
    emailID : String,
    chatID  : String,
    fromAdress : String,
    dateOf : String,
    header  : String,
    body : String
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

  class EmailTable(tag: Tag) extends Table[Email](tag, "emails") {

    def emailID = column[String]("emailID", O.PrimaryKey)
    def chatID  = column[String]("chatID")
    def fromAdress = column[String]("fromAdress")
    def dateOf  = column[String]("dateOf")
    def header = column[String]("header")
    def body  = column[String]("body")

    def fileIdFK = foreignKey("chatID", chatID, ChatObject.ChatTable)(_.chatID, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

    def * = (emailID, chatID, fromAdress,dateOf,header,body) <> (Email.tupled, Email.unapply)
  }

  class ToAdressTable(tag: Tag) extends Table[ToAdress](tag,"toadresses") {

    def toID = column[String]("toID", O.PrimaryKey)

    def emailID = column[String]("emailID")

    def username = column[String]("username")

    def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (toID, emailID, username) <> (ToAdress.tupled, ToAdress.unapply)
  }
  class CCTable(tag: Tag) extends Table[ToAdress](tag,"ccs") {

    def CCID = column[String]("CCID", O.PrimaryKey)

    def emailID = column[String]("emailID")

    def username = column[String]("username")

    def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (CCID, emailID, username) <> (ToAdress.tupled, ToAdress.unapply)
  }
  class BCCTable(tag: Tag) extends Table[ToAdress](tag,"bccs") {

    def BCCID = column[String]("BCCID", O.PrimaryKey)

    def emailID = column[String]("emailID")

    def username = column[String]("username")

    def fileIdFK = foreignKey("emailID", emailID, EmailTable)(_.emailID, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def * = (BCCID, emailID, username) <> (ToAdress.tupled, ToAdress.unapply)
  }

  lazy val EmailTable = TableQuery[EmailTable]

  lazy val ToAdressTable = TableQuery[ToAdressTable]

  lazy val CCTable = TableQuery[CCTable]

  lazy val BCCTable = TableQuery[BCCTable]


  def insertEmail(email:Email) = EmailTable += email

  val db = Database.forConfig("mysql")

  def execDB[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)
}
