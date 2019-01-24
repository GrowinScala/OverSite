package database.repository

import java.util.UUID.randomUUID

import api.dtos._
import database.mappings.ChatMappings._
import database.mappings.EmailMappings._
import database.mappings._
import definedStrings.DatabaseStrings._
import javax.inject.Inject
import slick.jdbc.MySQLProfile.api._
import database.repository.EmailRepositoryImpl
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

class ChatRepositoryImpl @Inject() (implicit val executionContext: ExecutionContext, db: Database) extends ChatRepository {

  /**
   * Aims to find an chatID already exists in the database
   * @param chatID Reference to an email conversation
   * @return True or False depending if the chatID exists or not
   */
  private def existChatID(chatID: String): Future[Boolean] = {

    val tableSearch = chatTable
      .filter(_.chatID === chatID)
      .result

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
      Future.sequence(seq.map(chatId =>
        db.run(emailIdsForSentEmails
          .filter(_._1 === chatId)
          .sortBy(_._3.reverse)
          .take(1)
          .result
          .headOption))))

    result.flatMap(futureSeqTriplets => futureSeqTriplets.map(seq =>
      seq.map { optionTripletStrings =>
        optionTripletStrings.getOrElse(TripletEmptyString) match {
          case (chatID, header, _) => MinimalInfoDTO(chatID, header)
        }
      }))

  }

  /**
   * Query that selects the emailIDs from the EmailTable that
   * are returned by the auxiliary query "queryUserName", filters by chatID inputed,
   * by the state "Sent", and sort by the date.
   */
  private def queryChat(userEmail: String, chatID: String, isTrash: Boolean): Query[EmailTable, EmailRow, Seq] = {
    emailTable
      .filter(_.emailID in auxEmailFilter(userEmail, isTrash))
      .filter(_.chatID === chatID)
      .filter(_.isTrash === isTrash)
      .sortBy(_.dateOf)
  }

  /** Function that selects emails through userName and chatID*/
  def getEmails(userEmail: String, chatID: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]] = {
    val queryResult = queryChat(userEmail, chatID, isTrash)
      .map(emailTable => (emailTable.emailID, emailTable.header))
      .result

    db.run(queryResult).map(seq => seq.map {
      case (id, header) => MinimalInfoDTO(id, header)
    })
  }

  /** Selects an email after filtering through chatID emailID*/
  def getEmail(userEmail: String, chatID: String, emailID: String, isTrash: Boolean): Future[Seq[EmailInfoDTO]] = {

    val queryTos = db.run(destinationEmailTable.filter(_.emailID === emailID).filter(_.destination === Destination.ToAddress).map(_.username).result)

    val queryResult = queryTos.map(seqTos =>
      queryChat(userEmail, chatID, isTrash)
        .filter(_.emailID === emailID)
        .map(table => (table.fromAddress, table.header, table.body, table.dateOf))
        .result.map(seq => seq.map {
          case (fromAddress, header, body, dateOf) => EmailInfoDTO(chatID, fromAddress, seqTos, header, body, dateOf)
        }))

    queryResult.flatMap(db.run(_))
  }

  def changeTrash(username: String, chatID: String, moveToTrash: Boolean) = {
    implicit val chatActions = new ChatRepositoryImpl()
    val emailActions = new EmailRepositoryImpl()

    val resultEmailTable = emailTable
      .filter(_.chatID === chatID)
      .map(_.emailID)

    val filteredEmailTable = emailTable
      .filter(_.emailID in resultEmailTable)
      .filter(_.fromAddress === username)
      .filter(_.isTrash === !moveToTrash)
      .map(_.emailID)

    val filteredDestinationTable = destinationEmailTable
      .filter(_.emailID in resultEmailTable)
      .filter(_.username === username)
      .filter(_.isTrash === !moveToTrash)
      .map(_.emailID)

    //println(Await.result(db.run(filteredEmailTable.size.result), Duration.Inf).toString)
    //println(Await.result(db.run(filteredDestinationTable.size.result), Duration.Inf).toString)
    db.run(filteredEmailTable.result).flatMap(seqID => Future.sequence(seqID.map(ID => emailActions.changeTrash(username, ID))))
    db.run(filteredDestinationTable.result).flatMap(seqID => Future.sequence(seqID.map(ID => emailActions.changeTrash(username, ID))))

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

  /** Auxiliary function*/
  private def queryUser(query: Query[(Rep[String], Rep[String]), (String, String), Seq]): Query[Rep[String], String, Seq] = {
    emailTable
      .filter(_.fromAddress in query.map { case (_, user) => user })
      .map(_.emailID)
      .union(destinationEmailTable.
        filter(_.username in query.map { case (_, user) => user })
        .map(_.emailID))
  }

  /**
   * Query to get the most recent email header from a chatID, from all chats that are supervised by an user
   * @param userEmail Identification of user by email
   * @return List of Chat IDs and respective headers
   */
  def getShares(userEmail: String): Future[Seq[MinimalInfoDTO]] = {

    val queryEmailID = shareTable
      .filter(_.toUser === userEmail)
      .map(shareTable => (shareTable.chatID, shareTable.fromUser))

    val queryChatId = emailTable.filter(_.chatID in queryEmailID.map { case (chatID, _) => chatID })
      .filter(_.emailID in queryUser(queryEmailID))
      .sortBy(_.dateOf)
      .map(emailTable => (emailTable.chatID, emailTable.header))
      .distinctOn(_._1)
      .result

    db.run(queryChatId).map(seq => seq.map {
      case (id, header) => MinimalInfoDTO(id, header)
    })

  }

  /** Query to get the list of allowed emails that are linked to the chatID that correspond to shareID */
  def getSharedEmails(userEmail: String, shareID: String): Future[Seq[MinimalInfoDTO]] = {

    val queryShareId = shareTable
      .filter(_.shareID === shareID)
      .filter(_.toUser === userEmail)
      .map(shareTable => (shareTable.chatID, shareTable.fromUser))

    val queryChatId = emailTable
      .filter(_.chatID in queryShareId.map { case (chatID, _) => chatID })
      .filter(_.emailID in queryUser(queryShareId))
      .sortBy(_.dateOf)
      .map(emailTable => (emailTable.emailID, emailTable.header))
      .result

    db.run(queryChatId).map(seq => seq.map {
      case (id, header) => MinimalInfoDTO(id, header)
    })
  }

  /**
   * Query to get the email, when shareID and emailID are provided
   * @return Share ID, Email ID, Chat ID, From address, To address, Header, Body, Date of the email wanted
   */
  def getSharedEmail(userEmail: String, shareID: String, emailID: String): Future[Seq[EmailInfoDTO]] = {

    val queryShareId = shareTable
      .filter(_.shareID === shareID)
      .filter(_.toUser === userEmail)
      .map(shareTable => (shareTable.chatID, shareTable.fromUser))

    val queryTos = db.run(destinationEmailTable
      .filter(_.emailID === emailID)
      .filter(_.destination === Destination.ToAddress)
      .map(_.username).result)

    val queryChatId = queryTos.map(seqTos => emailTable
      .filter(_.chatID in queryShareId.map { case (chatID, _) => chatID })
      .filter(_.emailID in queryUser(queryShareId))
      .filter(_.emailID === emailID)
      .map(table => (table.chatID, table.fromAddress, table.header, table.body, table.dateOf))
      .result.map(seq => seq.map {
        case (chatID, fromAddress, header, body, dateOf) =>
          EmailInfoDTO(chatID, fromAddress, seqTos, header, body, dateOf)
      }))

    queryChatId.flatMap(db.run(_))
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

}
