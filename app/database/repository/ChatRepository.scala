package database.repository

import api.dtos.{ CreateEmailDTO, CreateShareDTO, EmailInfoDTO, EmailMinimalInfoDTO }

import scala.concurrent.Future

trait ChatRepository {
  def insertChat(email: CreateEmailDTO, chatID: String): Future[String]
  def getInbox(userEmail: String): Future[Seq[EmailMinimalInfoDTO]]
  def getEmails(userEmail: String, chatID: String): Future[Seq[EmailMinimalInfoDTO]]
  def getEmail(userEmail: String, chatID: String, emailID: String): Future[Seq[EmailInfoDTO]]
  def insertPermission(from: String, share: CreateShareDTO): Future[String]
  def getShares(userEmail: String): Future[Seq[EmailMinimalInfoDTO]]
  def getSharedEmails(userEmail: String, shareID: String): Future[Seq[EmailMinimalInfoDTO]]
  def getSharedEmail(userEmail: String, shareID: String, emailID: String): Future[Seq[EmailInfoDTO]]
  def deletePermission(from: String, to: String, chatID: String): Future[Int]
}