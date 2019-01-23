package database.repository

import java.util.UUID.randomUUID

import api.dtos.{CreateEmailDTO, DraftInfoDTO, MinimalInfoDTO}
import database.mappings.DraftMappings._
import database.mappings.{Destination, DestinationDraftRow, DraftRow}
import definedStrings.ApiStrings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

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
      _ <- destinationDraftTable ++= draft.to.getOrElse(Seq("")).map(DestinationDraftRow(draftID, _, Destination.ToAddress))
      _ <- destinationDraftTable ++= draft.CC.getOrElse(Seq("")).map(DestinationDraftRow(draftID, _, Destination.CC))
      _ <- destinationDraftTable ++= draft.BCC.getOrElse(Seq("")).map(DestinationDraftRow(draftID, _, Destination.BCC))
    } yield draftID

    db.run(insertDraft.transactionally)
  }

  /** Function that update a draft deleting all the data related to that draftID replacing it with the new draft*/
  def updateDraft(draft: CreateEmailDTO, username: String, draftID: String): Future[String] = {

    val updateDraft = for {
      _ <- destinationDraftTable
        .filter(_.draftID === draftID)
        .delete

      _ <- draftTable
        .filter(_.draftID === draftID)
        .filter(_.username === username)
        .delete
    } yield insertDraft(username, draft)

    db.run(updateDraft.transactionally).flatten
  }

  /** Function that selects list of drafts through userName*/
  def getDrafts(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]] = {

    val queryDrafts = draftTable
      .filter(_.username === userEmail)
      .filter(_.isTrash === isTrash)
      .sortBy(_.dateOf.reverse)

    val queryResult = queryDrafts
      .map(draftTable => (draftTable.draftID, draftTable.header))
      .result

    db.run(queryResult).map(seq => seq.map {
      case (id, header) => MinimalInfoDTO(id, header)
    })
  }



  /**
   * Function that filter the emails, according to their draftID, if it is trash or not and userEmail
   * @param userEmail Identification of user by email
   * @param isTrash Boolean representing if the email required is trash or not
   * @param draftID Identification the a specific draft
   * @return All the details of the draft selected
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

    } yield draft.map{ case (draftID, username, header, body, dateOf) =>
        DraftInfoDTO(draftID, username, toSeq, ccSeq, bccSeq, header, body, dateOf)
    }
  }

  /**
   * Reaches a certain draft and turns it into an email, if it has any To, CC or BCC
   * @param username Identification of user by email
   * @param draftID Identification the a specific email
   * @return returns a Future of String, that is the emailID from the new inserted email
   */
  def takeDraftMakeSent(username: String, draftID: String, listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[String] = {

    val email = for {
      email <- draftTable
        .filter(_.draftID === draftID)
        .map(entry => (entry.chatID, entry.dateOf, entry.header, entry.body))
        .result
        .headOption

      _ <- destinationDraftTable
        .filter(_.draftID === draftID)
        .delete

      _ <- draftTable
        .filter(_.username === username)
        .filter(_.draftID === draftID)
        .delete

    } yield email.map {
      case (chatId, dateOf, header, body) =>
        CreateEmailDTO(Option(chatId), dateOf, header, body, Option(listTos), Option(listBCCs), Option(listCCs))
    }

    //Won´t insert in db because empty string is not a valid Date
    db.run(email.transactionally).flatMap(emailDTO =>
      emailActions.insertEmail(username, emailDTO.getOrElse(CreateEmailDTO(Option(EmptyString), EmptyString, EmptyString, EmptyString, Option(Seq(EmptyString)), Option(Seq(EmptyString)), Option(Seq(EmptyString)))))
    )

  }

  def destinations(username: String, draftID: String): Future[(Seq[String], Seq[String], Seq[String])] = {

    val auxQuery = destinationDraftTable
      .filter(_.draftID === draftID)

    for {
      listCCs <- db.run(auxQuery
        .filter(_.destination === Destination.CC)
        .map(_.username)
        .result)

      listBCCs <- db.run(auxQuery
        .filter(_.destination === Destination.BCC)
        .map(_.username)
        .result)

      listTos <- db.run(auxQuery
        .filter(_.destination === Destination.ToAddress)
        .map(_.username)
        .result)
    } yield (listTos, listBCCs, listCCs)
  }

  def hasDestination(listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[Boolean] = {
    Future.successful{listCCs.size + listBCCs.size + listTos.size > 0}
  }

  def moveInOutTrash(userEmail: String, draftID: String, trash: Boolean): Future[Int] = {

    val draftFilter = draftTable
      .filter(_.username === userEmail)
      .filter(_.draftID === draftID)
      .map(_.isTrash)

    db.run(draftFilter.update(trash))
  }
}
