package database.repository

import api.dtos.{ CreateEmailDTO, CreateShareDTO }

import scala.concurrent.Future

trait ChatRepository {
  def insertChat(email: CreateEmailDTO, chatID: String): Future[String]
  def getInbox(userEmail: String): Future[Seq[(String, String)]]
  def getEmails(userEmail: String, chatID: String): Future[Seq[(String, String)]]
  def getEmail(userEmail: String, chatID: String, emailID: String): Future[Seq[(String, String, String, String, String)]]
  def insertPermission(from: String, share: CreateShareDTO): Future[String]
  def getShares(userEmail: String): Future[Seq[(String, String)]]
  def getSharedEmails(userEmail: String, shareID: String): Future[Seq[(String, String)]]
  def getSharedEmail(userEmail: String, shareID: String, emailID: String): Future[Seq[(String, String, String, String, String, String)]]
  def deletePermission(from: String, to: String, chatID: String): Future[Int]
}