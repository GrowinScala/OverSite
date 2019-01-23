package database.repository

import java.util.UUID.randomUUID

import database.mappings.DraftMappings._
import api.dtos.{ CreateEmailDTO, EmailMinimalInfoDTO }
import database.mappings.{ Destination, DestinationDraftRow, DraftRow }
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._
import definedStrings.ApiStrings._

import scala.concurrent.{ ExecutionContext, Future }

class DraftRepositoryImpl @Inject() (implicit val executionContext: ExecutionContext, db: Database, emailActions: EmailRepositoryImpl)
  extends DraftRepository {

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
  def getDrafts(userEmail: String, isTrash: Boolean): Future[Seq[EmailMinimalInfoDTO]] = {

    val queryDrafts = draftTable.filter(_.username === userEmail).filter(_.isTrash === isTrash).sortBy(_.dateOf.reverse)

    val queryResult = queryDrafts
      .map(draftTable => (draftTable.draftID, draftTable.header))
      .result

    db.run(queryResult).map(seq => seq.map {
      case (id, header) => EmailMinimalInfoDTO(id, header)
    })
  }

  def updateDraft(draft: CreateEmailDTO, username: String, draftID: String): Future[String] = {

    val updateDraft = for {
      _ <- destinationDraftTable.filter(_.draftID === draftID).delete
      _ <- draftTable.filter(_.draftID === draftID).filter(_.username === username).delete
    } yield insertDraft(username, draft)

    db.run(updateDraft.transactionally).flatten
  }

  /**
   * Reaches a certain draft and turns it into an email,if it has some To, CC or BCC
   * @param username Identification of user by email
   * @param draftID Identification the a specific email
   * @return returns a Future of String, that is the emailID from the new inserted email
   */
  def takeDraftMakeSent(username: String, draftID: String, listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[String] = {

    val email = for {
      email <- draftTable.filter(_.draftID === draftID)
        .map(entry => (entry.chatID, entry.dateOf, entry.header, entry.body)).result.headOption

      _ <- destinationDraftTable.filter(_.draftID === draftID).delete
      _ <- draftTable.filter(_.username === username).filter(_.draftID === draftID).delete

    } yield email.map {
      case (chatId, dateOf, header, body) =>
        CreateEmailDTO(Option(chatId), dateOf, header, body, Option(listTos), Option(listBCCs), Option(listCCs))

    }
    db.run(email.transactionally).flatMap(emailDTO => emailActions.insertEmail(username, emailDTO.getOrElse(
      //Won´t insert in db because empty string is not a valid Date
      CreateEmailDTO(Option(EmptyString), EmptyString, EmptyString, EmptyString, Option(Seq(EmptyString)),
        Option(Seq(EmptyString)), Option(Seq(EmptyString))))))
  }

  def destinations(username: String, draftID: String): Future[(Seq[String], Seq[String], Seq[String])] = {

    val auxQuery = destinationDraftTable.filter(_.draftID === draftID)

    for {
      listCCs <- db.run(auxQuery
        .filter(_.destination === Destination.CC).map(_.username).result)

      listBCCs <- db.run(auxQuery
        .filter(_.destination === Destination.BCC).map(_.username).result)

      listTos <- db.run(auxQuery
        .filter(_.destination === Destination.ToAddress).map(_.username).result)
    } yield (listTos, listBCCs, listCCs)

  }

  def hasDestination(listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[Boolean] = {
    if (listCCs.size + listBCCs.size + listTos.size > 0)
      Future.successful(true)
    else
      Future.successful(false)
  }
}
