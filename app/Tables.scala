
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.MySQLDriver.api._

class Tables {

  case class Email(
    emailID : String,
    chatID  : String,
    fromAdress : String,
    dateOf : String,
    header  : String,
    body : String
    )



  class EmailTable(tag: Tag) extends Table[Email](tag, "email") {

    def emailID = column[String]("artist", O.PrimaryKey)
    def chatID  = column[String]("title")
    def fromAdress = column[String]("artist")
    def dateOf  = column[String]("title")
    def header = column[String]("artist")
    def body  = column[String]("title")
    //def fileIdFK =
    //                            filesTable Reference from the other table        _.fileID PK from the other table
    //foreignKey("chatID", chatID, filesTable)(_.fileId, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)


    def * = (emailID, chatID, fromAdress,dateOf,header,body) <> (Email.tupled, Email.unapply)
  }

}
