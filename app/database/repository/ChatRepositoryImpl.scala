package database.repository

import java.util.UUID.randomUUID

import api.dtos.{ CreateEmailDTO, CreateShareDTO, EmailInfoDTO, EmailMinimalInfoDTO }
import database.mappings.ChatMappings._
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable, draftTable, destinationDraftTable }
import database.mappings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._
import definedStrings.DatabaseStrings._

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

class ChatRepositoryImpl @Inject() (implicit val executionContext: ExecutionContext, db: Database) extends ChatRepository {

  /**
   * Aims to find an chatID already exists in the database
   * @param chatID Reference to an email conversation
   * @return True or False depending if the chatID exists or not
   */
  private def existChatID(chatID: String): Future[Boolean] = {
    val tableSearch = chatTable.filter(_.chatID === chatID).result
    db.run(tableSearch).map(_.length).map {
      case 1 => true
      case _ => false
    }
  }

  /**
   * Insert a chat into database
   * @param email email passed on json body
   * @param chatID chatID
   * @return
   */
  def insertChat(email: CreateEmailDTO, chatID: String): Future[String] = {
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
   * Query that search for all the emails which have a certain userName involved
   * @param userEmail the user identity
   * @return The sequence of emailIDS which userEmail is involved (to, from cc and bcc)
   */
  private def hasBeenSent(userEmail: String, isTrash: Boolean): Query[Rep[String], String, Seq] = {
    emailTable.filter(_.fromAddress === userEmail).filter(_.isTrash === isTrash).map(_.emailID)
      .union(toAddressTable.filter(_.username === userEmail).filter(_.isTrash === isTrash).map(_.emailID))
      .union(ccTable.filter(_.username === userEmail).filter(_.isTrash === isTrash).map(_.emailID))
      .union(bccTable.filter(_.username === userEmail).filter(_.isTrash === isTrash).map(_.emailID))
  }

  /**
   * Queries to find the inbox messages of an user,
   * @param userEmail user email
   * @return All the mails that have the username in "from", "to", "CC" and "BCC" categories
   */
  def getInbox(userEmail: String, isTrash: Boolean): Future[Seq[EmailMinimalInfoDTO]] = {

    val emailIdsForSentEmails = emailTable.filter(_.emailID in hasBeenSent(userEmail, isTrash))
      .map(entry => (entry.chatID, entry.header, entry.dateOf)).sortBy(_._3.reverse)

    val idsDistinctList = db.run(emailIdsForSentEmails.result).map(seq => seq.map(_._1).distinct)

    val result = idsDistinctList.map(seq =>
      seq.map(chatId =>
        db.run(emailIdsForSentEmails.filter(_._1 === chatId).sortBy(_._3.reverse).take(1).result.head)))

    result.map(seqTriplets => seqTriplets.map(x => Await.result(x.map { y => EmailMinimalInfoDTO(y._1, y._2) }, Duration.Inf)))
  }

  /**
   * Query that selects the emailIDs from the EmailTable that
   * are returned by the auxiliary query "queryUserName", filters by chatID inputed,
   * by the state "Sent", and sort by the date.
   */
  private def queryChat(userEmail: String, chatID: String, isTrash: Boolean): Query[EmailTable, EmailRow, Seq] = {
    val queryUserName = hasBeenSent(userEmail, isTrash)
    emailTable
      .filter(_.emailID in queryUserName)
      .filter(_.chatID === chatID)
      .filter(_.isTrash === isTrash)
      .sortBy(_.dateOf)
  }

  /** Function that selects emails through userName and chatID*/
  def getEmails(userEmail: String, chatID: String, isTrash: Boolean): Future[Seq[EmailMinimalInfoDTO]] = {
    val queryResult = queryChat(userEmail, chatID, isTrash)
      .map(emailTable => (emailTable.emailID, emailTable.header))
      .result

    db.run(queryResult).map(seq => seq.map {
      case (id, header) => EmailMinimalInfoDTO(id, header)
    })
  }

  /** Selects an email after filtering through chatID emailID*/
  def getEmail(userEmail: String, chatID: String, emailID: String, isTrash: Boolean): Future[Seq[EmailInfoDTO]] = {
    val queryResult = queryChat(userEmail, chatID, isTrash)
      .filter(_.emailID === emailID)
      //Since every email with sent==true is obligated to have an ToID,
      // the following join has the same effect as joinLeft
      .joinLeft(toAddressTable).on(_.emailID === _.emailID)
      //Order of the following map: fromAddress, username(from toAddress table), header, body,  dateOf
      .map(table => (table._1.fromAddress, table._2.map(_.username).getOrElse(NoneString), table._1.header, table._1.body, table._1.dateOf))
      .result.map(seq => seq.map {
        case (fromAddress, username, header, body, dateOf) =>
          EmailInfoDTO(chatID, fromAddress, username, header, body, dateOf)
      })
    db.run(queryResult)
  }
  /**
   *  Authorize an user to have access to a conversation
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

  /** Auxiliary function*/
  private def queryUser(query: Query[(Rep[String], Rep[String]), (String, String), Seq]) = {
    emailTable.filter(_.fromAddress in query.map { case (_, user) => user }).map(_.emailID)
      .union(toAddressTable.filter(_.username in query.map { case (_, user) => user }).map(_.emailID))
      .union(ccTable.filter(_.username in query.map { case (_, user) => user }).map(_.emailID))
      .union(bccTable.filter(_.username in query.map { case (_, user) => user }).map(_.emailID))
  }
  /**
   * Query to get the most recent email header from a chatID, from all chats that are supervised by an user
   * @param userEmail Identification of user by email
   * @return List of Chat IDs and respective headers
   */
  def getShares(userEmail: String): Future[Seq[EmailMinimalInfoDTO]] = {

    val queryEmailId = shareTable.filter(_.toID === userEmail)
      .map(shareTable => (shareTable.chatID, shareTable.fromUser))

    val queryFromUser = queryUser(queryEmailId)

    val queryChatId = emailTable.filter(_.chatID in queryEmailId.map { case (chatid, _) => chatid })
      .filter(_.emailID in queryFromUser)
      .sortBy(_.dateOf)
      .map(emailTable => (emailTable.chatID, emailTable.header)).distinctOn(_._1)
      .result
    db.run(queryChatId).map(seq => seq.map { case (id, header) => EmailMinimalInfoDTO(id, header) })

  }

  /** Query to get the list of allowed emails that are linked to the chatID that correspond to shareID */
  def getSharedEmails(userEmail: String, shareID: String): Future[Seq[EmailMinimalInfoDTO]] = {
    val queryShareId = shareTable.filter(_.shareID === shareID)
      .filter(_.toID === userEmail)
      .map(shareTable => (shareTable.chatID, shareTable.fromUser))

    val queryFromUser = queryUser(queryShareId)

    val queryChatId = emailTable.filter(_.chatID in queryShareId.map(x => x._1))
      .filter(_.emailID in queryFromUser)
      .sortBy(_.dateOf)
      .map(emailTable => (emailTable.emailID, emailTable.header))
      .result
    db.run(queryChatId).map(seq => seq.map { case (id, header) => EmailMinimalInfoDTO(id, header) })
  }

  /**
   * Query to get the email, when shareID and emailID are provided
   * @return Share ID, Email ID, Chat ID, From address, To address, Header, Body, Date of the email wanted
   */
  def getSharedEmail(userEmail: String, shareID: String, emailID: String): Future[Seq[EmailInfoDTO]] = {

    val queryShareId = shareTable.filter(_.shareID === shareID)
      .filter(_.toID === userEmail)
      .map(shareTable => (shareTable.chatID, shareTable.fromUser))

    val queryFromUser = queryUser(queryShareId)

    val queryChatId = emailTable.filter(_.chatID in queryShareId.map(x => x._1))
      .filter(_.emailID in queryFromUser)
      .filter(_.emailID === emailID)
      .joinLeft(toAddressTable).on(_.emailID === _.emailID)
      .map(table => (table._1.chatID, table._1.fromAddress, table._2.map(_.username).getOrElse(NoneString), table._1.header, table._1.body, table._1.dateOf))
      .result
    db.run(queryChatId).map(seq => seq.map { case (chatID, fromAddress, username, header, body, dateOf) => EmailInfoDTO(chatID, fromAddress, username, header, body, dateOf) })
  }

  /**
   * Remove permission from an user to another user, related to a specific chatID
   * @return Delete of row that mark the permission in cause
   */
  def deletePermission(from: String, to: String, chatID: String): Future[Int] = {
    val deletePermissionTable = shareTable.filter(p => (p.fromUser === from) && (p.toID === to) && (p.chatID === chatID)).delete
    db.run(deletePermissionTable)
  }

  //TODO def function T:04
}
