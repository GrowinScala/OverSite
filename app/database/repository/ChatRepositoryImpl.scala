package database.repository

import java.util.UUID.randomUUID

import api.dtos.{ CreateEmailDTO, CreateShareDTO, MinimalInfoDTO, MinimalShareInfoDTO }
import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.mappings._
import database.properties.DBProperties
import definedStrings.DatabaseStrings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

class ChatRepositoryImpl @Inject() (dbClass: DBProperties)(implicit val executionContext: ExecutionContext) extends ChatRepository {
  val db = dbClass.db

  /**
   * Insert a chat into database
   * @param email email passed on json body
   * @param chatID chatID
   * @return
   */
  def insertChat(email: CreateEmailDTO, chatID: String): Future[String] = {

    /**
     * Aims to find an chatID already exists in the database
     * @param chatID Reference to an email conversation
     * @return True or False depending if the chatID exists or not
     */
    def existChatID(chatID: String): Future[Boolean] = {

      val tableSearch = chatTable
        .filter(_.chatID === chatID)
        .result

      db.run(tableSearch).map(_.length).map {
        case 1 => true
        case _ => false
      }
    }

    val randomChatID = randomUUID().toString

    existChatID(chatID).flatMap {
      case true => Future { chatID }
      case false =>
        for {
          _ <- db.run(chatTable += ChatRow(randomChatID, email.header))
        } yield randomChatID
    }

  }

  /**
   * Queries to find the inbox messages of an user,
   * @param userEmail user email
   * @return All the mails that have the username in "from", "to", "CC" and "BCC" categories
   */
  def getInbox(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]] = {

    val emailIdsForSentEmails = emailTable
      .filter(_.emailID in auxEmailFilter(userEmail, isTrash))
      .map(entry => (entry.chatID, entry.header, entry.dateOf))
      .sortBy(_._3.reverse)

    val idsDistinctList = db.run(emailIdsForSentEmails.result)
      .map(seq => seq.map(_._1).distinct)

    val result = idsDistinctList.map(seq =>
      Future.sequence(seq.map(chatID =>
        db.run(emailIdsForSentEmails
          .filter(_._1 === chatID)
          .sortBy(_._3.reverse)
          .take(num = 1)
          .result
          .headOption))))

    result.flatMap(futureSeqTriplets => futureSeqTriplets.map(seq =>
      seq.map { optionTripletStrings =>
        optionTripletStrings.getOrElse(TripletEmptyString) match {
          case (chatID, header, _) => MinimalInfoDTO(chatID, header)
        }
      }))

  }

  /** Function that selects emails through userName and chatID*/
  def getEmails(userEmail: String, chatID: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]] = {

    /**
     * Query that selects the emailIDs from the EmailTable that
     * are returned by the auxiliary query "queryUserName", filters by chatID inputed,
     * by the state "Sent", and sort by the date.
     */
    def queryChat(userEmail: String, chatID: String, isTrash: Boolean): Query[EmailTable, EmailRow, Seq] = {
      emailTable
        .filter(_.emailID in auxEmailFilter(userEmail, isTrash))
        .filter(_.chatID === chatID)
        .filter(_.isTrash === isTrash)
        .sortBy(_.dateOf)
    }

    val queryResult = queryChat(userEmail, chatID, isTrash)
      .map(emailTable => (emailTable.emailID, emailTable.header))
      .result

    db.run(queryResult).map(seq => seq.map {
      case (id, header) => MinimalInfoDTO(id, header)
    })
  }

  /**
   * Function that moves all the mails from a certain chatID to trash or vice versa
   * @param username Identification of the username
   * @param chatID Identification of the chat
   * @param moveToTrash Identification of the sense of movement
   * @return
   */
  def changeTrash(username: String, chatID: String, moveToTrash: Boolean): Future[Int] = {

    val resultEmailTable = db.run(emailTable
      .filter(_.chatID === chatID)
      .map(_.emailID).result)

    for {
      emailResult <- resultEmailTable

      updateEmailResult <- db.run(
        emailTable
          .filter(_.emailID inSet emailResult)
          .filter(_.fromAddress === username)
          .filter(_.isTrash === !moveToTrash)
          .map(_.isTrash)
          .update(moveToTrash))

      updateDestinationResult <- db.run(
        destinationEmailTable
          .filter(_.emailID inSet emailResult)
          .filter(_.username === username)
          .filter(_.isTrash === !moveToTrash)
          .map(_.isTrash)
          .update(moveToTrash))

    } yield updateEmailResult + updateDestinationResult
  }

  /**
   * Authorize an user to have access to a conversation
   * @param from User that concedes permission
   * @param share User that grants the authorization
   * @return Insert permission to an user
   */
  def insertPermission(from: String, share: CreateShareDTO): Future[String] = {
    val shareID = randomUUID().toString

    for {
      _ <- db.run(shareTable += ShareRow(shareID, share.chatID, from, share.supervisor))

    } yield shareID
  }

  /**
   * Query to get the most recent email header from a chatID, from all chats that are supervised by an user
   * @param userEmail Identification of user by email
   * @return List of Chat IDs and respective headers
   */
  def getShares(userEmail: String): Future[Seq[MinimalShareInfoDTO]] = {

    val queryEmailID = shareTable
      .filter(_.toUser === userEmail)
      .join(emailTable).on(_.chatID === _.chatID)
      .map { case (share, email) => (share.shareID, share.fromUser, email.emailID, email.dateOf, email.header) }

    val idsDistinctList = db.run(queryEmailID.result)
      .map(seq => seq.map(_._1).distinct)

    val result = idsDistinctList.map(seq =>
      Future.sequence(seq.map(shareID =>
        db.run(queryEmailID
          .filter(_._1 === shareID)
          .filter(x => x._3 in querySharesAux(x._2))
          .sortBy(_._4.reverse)
          .take(num = 1)
          .result
          .headOption))))

    result.flatMap(futureSeqTriplets => futureSeqTriplets.map(seq =>
      seq.map { optionTripletStrings =>
        optionTripletStrings.getOrElse((EmptyString, EmptyString, EmptyString, EmptyString, EmptyString)) match {
          case (shareID, fromUser, _, _, header) => MinimalShareInfoDTO(shareID, fromUser, header)
        }
      }))
  }

  /** Query to get the list of allowed emails that are linked to the chatID that correspond to shareID */
  def getSharedEmails(userEmail: String, shareID: String): Future[Seq[MinimalInfoDTO]] = {

    val queryShareId = shareTable
      .filter(_.shareID === shareID)
      .filter(_.toUser === userEmail)
      .join(emailTable).on(_.chatID === _.chatID)
      .map { case (share, email) => (share.fromUser, email.emailID, email.dateOf, email.header) }

    val result = db.run(queryShareId
      .filter(x => x._2 in querySharesAux(x._1))
      .sortBy(_._3.reverse)
      .result)

    result.map(futureSeqTriplets => futureSeqTriplets.map {
      case (_, emailID, _, header) => MinimalInfoDTO(emailID, header)
    })
  }

  /**
   * Remove permission from an user to another user, related to a specific chatID
   * @return Delete of row that mark the permission in cause
   */
  def deletePermission(from: String, to: String, chatID: String): Future[Int] = {
    val deletePermissionTable = shareTable
      .filter(_.fromUser === from)
      .filter(_.toUser === to)
      .filter(_.chatID === chatID)
      .delete

    db.run(deletePermissionTable)
  }

  /**
   * Query that search for all the emails which have a certain userName involved
   * @param userEmail the user identity
   * @return The sequence of emailIDS which userEmail is involved (to, from cc and bcc)
   */
  private def auxEmailFilter(userEmail: String, isTrash: Boolean): Query[Rep[String], String, Seq] = {

    emailTable
      .filter(_.fromAddress === userEmail)
      .filter(_.isTrash === isTrash)
      .map(_.emailID)
      .union(destinationEmailTable
        .filter(_.username === userEmail)
        .filter(_.isTrash === isTrash)
        .map(_.emailID))
  }

  private def querySharesAux(userEmail: Rep[String]): Query[Rep[String], String, Seq] = {
    emailTable
      .filter(_.fromAddress === userEmail)
      .map(_.emailID)
      .union(destinationEmailTable
        .filter(_.username === userEmail)
        .map(_.emailID))
  }

}
