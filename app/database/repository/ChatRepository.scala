package database.repository

import api.dtos._

import scala.concurrent.Future

trait ChatRepository {

  def insertChat(email: CreateEmailDTO, chatID: String): Future[String]
  def getInbox(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]]
  def getEmails(userEmail: String, chatID: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]]
  def changeTrash(username: String, chatID: String, moveToTrash: Boolean): Future[Int]
  def insertPermission(from: String, share: CreateShareDTO): Future[String]
  def getShares(userEmail: String): Future[Seq[MinimalShareInfoDTO]]
  def getSharedEmails(userEmail: String, shareID: String): Future[Seq[MinimalInfoDTO]]
  def deletePermission(from: String, to: String, chatID: String): Future[Int]

}