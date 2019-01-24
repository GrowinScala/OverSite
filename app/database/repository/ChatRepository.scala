package database.repository

import api.dtos.{ CreateEmailDTO, CreateShareDTO, EmailInfoDTO, MinimalInfoDTO }

import scala.concurrent.Future

trait ChatRepository {

  def insertChat(email: CreateEmailDTO, chatID: String): Future[String]
  def getInbox(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]]
  def getEmails(userEmail: String, chatID: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]]
  def getEmail(userEmail: String, chatID: String, emailID: String, isTrash: Boolean): Future[Seq[EmailInfoDTO]]
  def insertPermission(from: String, share: CreateShareDTO): Future[String]
  def getShares(userEmail: String): Future[Seq[MinimalInfoDTO]]
  def getSharedEmails(userEmail: String, shareID: String): Future[Seq[MinimalInfoDTO]]
  def getSharedEmail(userEmail: String, shareID: String, emailID: String): Future[Seq[EmailInfoDTO]]
  def deletePermission(from: String, to: String, chatID: String): Future[Int]

}