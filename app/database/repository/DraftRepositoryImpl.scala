package database.repository

import java.util.UUID.randomUUID

import api.dtos.{CreateEmailDTO, DraftInfoDTO, MinimalInfoDTO}
import database.mappings.EmailMappings.{draftTable, _}
import database.mappings.{Destination, DestinationDraftRow, DraftRow}
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class DraftRepositoryImpl @Inject() (implicit val executionContext: ExecutionContext, db: Database) extends DraftRepository {

  /**
   * Inserts a draft in the database
   * @return Generated chat ID
   */
  def insertDraft(username: String, draft: CreateEmailDTO): Future[String] = {

    val draftID = randomUUID().toString

    val insertDraft = for {
      _ <- draftTable += DraftRow(draftID, draft.chatID.getOrElse(""), username, draft.dateOf, draft.header, draft.body, isTrash = false)
      _ <- destinationDraftTable ++= draft.to.getOrElse(Seq()).map(DestinationDraftRow(draftID, _, Destination.ToAddress))
      _ <- destinationDraftTable ++= draft.CC.getOrElse(Seq()).map(DestinationDraftRow(draftID, _, Destination.CC))
      _ <- destinationDraftTable ++= draft.BCC.getOrElse(Seq()).map(DestinationDraftRow(draftID, _, Destination.BCC))
    } yield draftID

    db.run(insertDraft.transactionally)
  }

  /** Function that selects drafts through userName*/
  def getDrafts(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]] = {

    val queryDrafts = draftTable.filter(_.username === userEmail).filter(_.isTrash === isTrash).sortBy(_.dateOf.reverse)

    val queryResult = queryDrafts
      .map(draftTable => (draftTable.draftID, draftTable.header))
      .result

    db.run(queryResult).map(seq => seq.map {
      case (id, header) => MinimalInfoDTO(id, header)
    })
  }

  def updateDraft(draft: CreateEmailDTO, username: String, draftID: String): Future[String] = {

    val updateDraft = for {
      _ <- destinationDraftTable.filter(_.draftID === draftID).delete
      _ <- draftTable.filter(_.draftID === draftID).delete
    } yield insertDraft(username, draft)

    db.run(updateDraft.transactionally).flatten
  }

  /**
   * Function that filter the emails, according to their status and emailID
   * (joinLeft and getOrElse is used to embrace the 3 possible status, however
   * join and no getOrElse would be more appropriate for "sent" and "received")
   * @param userEmail Identification of user by email
   * @param status Possible status: "sent", "received", "trash" or "empty"
   * @param emailID Identification the a specific email
   * @return All the details of the email selected
   */
  def getDraft(userEmail: String, isTrash: Boolean, draftID: String): Future[Seq[DraftInfoDTO]] = {

    val queryResult = draftTable
      .filter(_.username === userEmail)
      .filter(_.draftID === draftID)
      .filter(_.isTrash === isTrash)

    val queryDestinationResult = destinationDraftTable
      .filter(_.draftID in queryResult.map(_.draftID))

    for {
      toSeq <- db.run(queryDestinationResult
        .filter(_.destination === Destination.ToAddress).map(_.username).result)

      ccSeq <- db.run(queryDestinationResult
        .filter(_.destination === Destination.CC).map(_.username).result)

      bccSeq <- db.run(queryDestinationResult
        .filter(_.destination === Destination.BCC).map(_.username).result)

      draft <- db.run(queryResult
        .map(table => (table.draftID, table.username, table.header, table.body, table.dateOf))
        .result)

    } yield draft.map(draftRow =>
      DraftInfoDTO(draftRow._1, draftRow._2, toSeq, ccSeq, bccSeq, draftRow._3, draftRow._4, draftRow._5))
  }

  /*
  def takeDraftMakeSent(username: String, draftID : String): Future[Int] = {

  }*/

  /*
  /**
   * Reaches a certain email drafted and send it
   * @param userName Identification of user by email
   * @param draftID Identification the a specific email
   * @return returns a Future of Int with the number of drafts turn to emails
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
  */
}
