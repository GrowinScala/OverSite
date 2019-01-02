package actions

import api.dtos.{ CreateEmailDTO, CreateUserDTO }
import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.repository.{ ChatRepository, EmailRepository, UserRepository }
import javax.inject.Inject
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class EmailActions @Inject() (implicit val executionContext: ExecutionContext, implicit val db: Database, chatActions: ChatRepository) {

  val userActions = new UserRepository()
  val emailActions = new EmailRepository()

  def createFilesTable = {
    db.run(emailTable.schema.create)
    db.run(chatTable.schema.create)
    db.run(toAddressTable.schema.create)
    db.run(ccTable.schema.create)
    db.run(bccTable.schema.create)
  }

  def dropFilesTable = {
    db.run(emailTable.schema.drop)
    db.run(chatTable.schema.drop)
    db.run(toAddressTable.schema.drop)
    db.run(ccTable.schema.drop)
    db.run(bccTable.schema.drop)
  }

  def deleteRowsTable = {
    db.run(emailTable.delete)
    db.run(chatTable.delete)
    db.run(toAddressTable.delete)
    db.run(ccTable.delete)
    db.run(bccTable.delete)
  }

  /** Insertion of an e-mail to the database and respective verification of  insertion on email table */
  def insertEmailTest(user: String, email: CreateEmailDTO) = {
    Await.result(emailActions.insertEmail(user, email), Duration("10 seconds"))

    val emailFilter = emailTable.filter(_.dateOf === email.dateOf)
      .filter(_.header === email.header)
      .filter(_.body === email.body)
      .filter(_.sent === email.sendNow).result

    Await.result(db.run(emailFilter).map(_.nonEmpty), Duration("10 seconds"))
  }

  /** Insertion of an e-mail to the database and respective verification of  insertion on chat table */
  def insertChatTableTest(user: String, email: CreateEmailDTO) = {
    Await.result(emailActions.insertEmail(user, email), Duration("10 seconds"))
    //Filtering with the non optional parameters of an email
    val chatFilter = chatTable.filter(_.header === email.header).result

    Await.result(db.run(chatFilter).map(_.nonEmpty), Duration("10 seconds"))
    //db.run(emailFilter).map(_.nonEmpty)
  }

  /** Insertion of an e-mail to the database and respective verification of  insertion on toaddresses table */
  def insertToAddressTableTest(user: String, email: CreateEmailDTO) = {
    Await.result(emailActions.insertEmail(user, email), Duration("10 seconds"))
    //Filtering with the non optional parameters of an email
    val toAddressFilter = toAddressTable.result
    email.to.isEmpty match {
      case false => Await.result(db.run(toAddressFilter).map(_.nonEmpty), Duration("10 seconds"))
      case _ => true
    }
  }

  /** Insertion of an e-mail to the database and respective verification of  insertion on BCC table */
  def insertBCCTableTest(user: String, email: CreateEmailDTO) = {
    Await.result(emailActions.insertEmail(user, email), Duration("10 seconds"))
    //Filtering with the non optional parameters of an email
    val BCCFilter = bccTable.result
    email.BCC.isEmpty match {
      case false => Await.result(db.run(BCCFilter).map(_.nonEmpty), Duration("10 seconds"))
      case _ => true
    }
  }

  /** Insertion of an e-mail to the database and respective verification of  insertion on CC table */
  def insertCCTableTest(user: String, email: CreateEmailDTO) = {
    Await.result(emailActions.insertEmail(user, email), Duration("10 seconds"))
    //Filtering with the non optional parameters of an email
    val CCFilter = ccTable.result
    email.CC.isEmpty match {
      case false => Await.result(db.run(CCFilter).map(_.nonEmpty), Duration("10 seconds"))
      case _ => true
    }
  }

  /** Insertion of an e-mail to the database and respective verification of the function getEmails*/
  def getEmailsTest(user: String, email: CreateEmailDTO, status: String) = {
    Await.result(emailActions.insertEmail(user, email), Duration("10 seconds"))
    Await.result(emailActions.getEmails(user, status).map(_.nonEmpty), Duration("10 seconds"))
  }

  /** Insertion of an e-mail to the database and respective verification of the function getEmail**/
  def getEmailTest(user: String, email: CreateEmailDTO, status: String) = {
    Await.result(emailActions.insertEmail(user, email), Duration("10 seconds"))
    val auxEmailID = Await.result(emailActions.getEmails(user, status).map(_.head._1), Duration("10 seconds"))
    Await.result(emailActions.getEmail(user, status, auxEmailID).map(_.nonEmpty), Duration("10 seconds"))
  }

  /** Insertion of an e-mail to the database and respective change of status, from drated to an sent email **/
  def takeDraftMakeSentTest(user: String, email: CreateEmailDTO) = {
    Await.result(emailActions.insertEmail(user, email), Duration("10 seconds"))
    val auxEmailID = Await.result(emailActions.getEmails(user, "draft").map(_.head._1), Duration("10 seconds"))
    //Update the email from drafted to an sent email
    Await.result(emailActions.takeDraftMakeSent(user, auxEmailID), Duration("10 seconds"))
    Await.result(emailActions.getEmails(user, "sent").map(_.nonEmpty), Duration("10 seconds"))
  }

}
