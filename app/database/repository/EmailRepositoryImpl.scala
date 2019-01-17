
package database.repository

import java.util.UUID.randomUUID

import api.dtos.{ CreateEmailDTO, EmailInfoDTO, EmailMinimalInfoDTO }
import database.mappings.EmailMappings.{ emailTable, _ }
import database.mappings._
import definedStrings.ApiStrings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

/**  Class that receives a db path */
class EmailRepositoryImpl @Inject() (implicit val executionContext: ExecutionContext, db: Database, chatActions: ChatRepositoryImpl) extends EmailRepository {

  /**
   * Inserts an email in the database
   * @return Generated chat ID
   */
  def insertEmail(username: String, email: CreateEmailDTO): Future[String] = {
    val randomEmailID = randomUUID().toString
    val isDraft = if (hasSenderAddress(email.to)) email.sendNow
    else false

    val chatId = chatActions.insertChat(email, email.chatID.getOrElse(randomUUID().toString))
    chatId.flatMap(id => {
      val insertEmail = for {
        _ <- emailTable += EmailRow(randomEmailID, id, username, email.dateOf, email.header, email.body, isDraft, isTrash = false)
        _ <- toAddressTable ++= email.to.getOrElse(Seq()).map(ToAddressRow(randomUUID().toString, randomEmailID, _, isTrash = false))
        _ <- ccTable ++= email.CC.getOrElse(Seq()).map(CCRow(randomUUID().toString, randomEmailID, _, isTrash = false))
        _ <- bccTable ++= email.BCC.getOrElse(Seq()).map(BCCRow(randomUUID().toString, randomEmailID, _, isTrash = false))
      } yield id

      db.run(insertEmail.transactionally)
    })
  }

  /**
   * Auxiliary function that supports getEmails and getEmail
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received", "draft" and "trash"
   * @return Return different queries taking into account the status
   */

  private def auxGetEmails(userEmail: String, status: String): Query[EmailTable, EmailRow, Seq] = {
    status match {
      case EndPointTrash =>
        val queryTrashEmailIds = emailTable.filter(_.fromAddress === userEmail).filter(_.isTrash === true).map(_.emailID)
          .union(toAddressTable.filter(_.username === userEmail).filter(_.isTrash === true).map(_.emailID))
          .union(ccTable.filter(_.username === userEmail).filter(_.isTrash === true).map(_.emailID))
          .union(bccTable.filter(_.username === userEmail).filter(_.isTrash === true).map(_.emailID))

        emailTable.filter(_.emailID in queryTrashEmailIds)
          .sortBy(_.dateOf)

      case EndPointSent =>
        emailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === true)
          .filter(_.isTrash === false)
          .sortBy(_.dateOf)

      case EndPointReceived =>
        val queryReceivedEmailIds = toAddressTable
          .filter(_.username === userEmail).map(_.emailID)
          .union(ccTable.filter(_.username === userEmail).map(_.emailID))
          .union(bccTable.filter(_.username === userEmail).map(_.emailID))

        emailTable.filter(_.emailID in queryReceivedEmailIds)
          .filter(_.sent === true)
          .filter(_.isTrash === false)
          .sortBy(_.dateOf)

      case EndPointDraft =>
        emailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === false)
          .filter(_.isTrash === false)
          .sortBy(_.dateOf)

      case EndPointNoFilter =>
        emailTable.filter(_.fromAddress === userEmail)
          .filter(_.sent === true)
          .filter(_.isTrash === false)
          .sortBy(_.dateOf)
    }
  }

  /**
   * Function that filter the emails "sent", "received", "draft", "trash" and "noFilter"
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received", "draft", "trash" and "noFilter"
   * @return List of emailIDs and corresponding header
   */
  def getEmails(userEmail: String, status: String): Future[Seq[EmailMinimalInfoDTO]] = {
    val queryResult = auxGetEmails(userEmail, status)
      .map(emailTable => (emailTable.emailID, emailTable.header))
      .result
    db.run(queryResult).map(seq => seq.map {
      case (emailId, header) =>
        EmailMinimalInfoDTO(emailId, header)
    })
  }
  /**
   * Function that filter the emails, according to their status and emailID
   * (joinLeft and getOrElse is used to embrace the 3 possible status, however
   * join and no getOrElse would be more appropriate for "sent" and "received")
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received", "draft", "trash" or "empty"
   * @param emailID Identification the a specific email
   * @return All the details of the email selected
   */
  def getEmail(userEmail: String, status: String, emailID: String): Future[Seq[EmailInfoDTO]] = {
    val queryResult = auxGetEmails(userEmail, status)
      .filter(_.emailID === emailID)
      .joinLeft(toAddressTable).on(_.emailID === _.emailID)
      .map(table => (table._1.chatID, table._1.fromAddress, table._2.map(_.username).getOrElse(EmptyString), table._1.header, table._1.body, table._1.dateOf))
      .result

    db.run(queryResult).map(seq => seq.map {
      case (chatID, fromAddress, username, header, body, dateOf) =>
        EmailInfoDTO(chatID, fromAddress, username, header, body, dateOf)
    })
  }

  private def hasSenderAddress(to: Option[Seq[String]]): Boolean = {
    to.getOrElse(Seq()).nonEmpty
  }

  /**
   * Reaches a certain email drafted and send it
   * @param userName Identification of user by email
   * @param emailID Identification the a specific email
   * @return returns a Future of Int with the number of emails undrafted
   */
  def takeDraftMakeSent(userName: String, emailID: String): Future[Int] = {

    val hasToAddress = toAddressTable.filter(_.emailID === emailID).result

    val toSent = emailTable.filter(emailTable => (emailTable.emailID === emailID) && (emailTable.fromAddress === userName))
      .filter(_.isTrash === false)
      .filter(_.sent === false)
      .map(_.sent)
      .update(true)

    db.run(hasToAddress).map(_.length).flatMap {
      case 1 => db.run(toSent)
      case _ => Future { 0 }
    }
  }

  /**
   * It changes the status of an email to trash or out of trash
   * @param userName Identification of user by email
   * @param emailID Identification the a specific email
   * @return
   */
  def changeTrash(userName: String, emailID: String): Future[Int] = {
    val filteredEmailTable = emailTable.filter(_.emailID === emailID).filter(_.fromAddress === userName).map(_.isTrash)
    val currentEmailStatus = db.run(filteredEmailTable.result.headOption)

    val filteredToAddressTable = toAddressTable.filter(_.emailID === emailID).filter(_.username === userName).map(_.isTrash)
    val currentToStatus = db.run(filteredToAddressTable.result.headOption)

    val filteredCCAddressTable = ccTable.filter(_.emailID === emailID).filter(_.username === userName).map(_.isTrash)
    val currentCCStatus = db.run(filteredCCAddressTable.result.headOption)

    val filteredBCCAddressTable = bccTable.filter(_.emailID === emailID).filter(_.username === userName).map(_.isTrash)
    val currentBCCStatus = db.run(filteredBCCAddressTable.result.headOption)

    for {
      resultEmailTable <- currentEmailStatus.map(status =>
        filteredEmailTable.update(!status.getOrElse(true))).flatMap(db.run(_))

      resultToAddressTable <- currentToStatus.map(status =>
        filteredToAddressTable.update(!status.getOrElse(true))).flatMap(db.run(_))

      resultCCTable <- currentCCStatus.map(status =>
        filteredCCAddressTable.update(!status.getOrElse(true))).flatMap(db.run(_))

      resultBCCTable <- currentBCCStatus.map(status =>
        filteredBCCAddressTable.update(!status.getOrElse(true))).flatMap(db.run(_))

    } yield resultEmailTable + resultToAddressTable + resultCCTable + resultBCCTable
  }

  //TODO COMMENT FUNCTION
  def updateDraft(userName: String, emailID: String, draft: CreateEmailDTO): Future[Int] = {

    /** Update emailTable*/
    val toSent = emailTable
      .filter(_.emailID === emailID)
      .filter(_.fromAddress === userName)
      .filter(_.sent === false)
      .filter(_.isTrash === false)

    //.filter(_.chatID in queryEmailId.map { case (chatid, _) => chatid })
    for {
      action1 <- db.run(toSent.map(_.dateOf).update(draft.dateOf))
      action2 <- db.run(toSent.map(_.header).update(draft.header))
      action3 <- db.run(toSent.map(_.sent).update(draft.sendNow))
      action4 <- db.run(toSent.map(_.isTrash).update(false))
    } yield action1 + action2 + action3 + action4

    /** Update toAddressTable*/
    /** Loop that returns the toIDs of users that remain in the update */

    //draft.to.getOrElse(Seq("")).map{x=>
    //if (for{result<- db.run(toAddressTable.filter(_.emailID in toSent.map(_.emailID)).filter(_.username===x))}yield result)

    //}
    /*
    val toAddressAux = for {
    toUser <- draft.to.getOrElse(Seq(""))

    listRemainId <- toAddressTable.filter(_.emailID in toSent.map(_.emailID)).filter(_.username === toUser).map(_.toID)
    }yield listRemainId

    val toAddressesDelete = toAddressTable.filter(_.emailID in toSent.map(_.emailID)).filter(_.toID in toAddressAux)
*/
  }
}
