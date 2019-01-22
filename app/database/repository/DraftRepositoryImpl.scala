package database.repository

import java.util.UUID.randomUUID

import database.mappings.EmailMappings._
import api.dtos.CreateEmailDTO
import database.mappings.{ Destination, DestinationDraftRow, DraftRow }
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

class DraftRepositoryImpl @Inject() (implicit val executionContext: ExecutionContext, db: Database) extends DraftRepository {

  def updateDraft(draft: CreateEmailDTO, username: String, draftID: String): Future[String] = {

    val updateDraft = for {
      _ <- destinationDraftTable.filter(_.draftID === draftID).delete
      _ <- draftTable.filter(_.draftID === draftID).delete
    } yield insertDraft(username, draft)

    db.run(updateDraft.transactionally).flatten

  }

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
