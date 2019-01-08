package database.repository

import api.dtos.{ CreateEmailDTO, EmailInfoDTO, EmailMinimalInfoDTO }

import scala.concurrent.Future

trait EmailRepository {

  def insertEmail(username: String, email: CreateEmailDTO): Future[String]
  def getEmails(userEmail: String, status: String): Future[Seq[EmailMinimalInfoDTO]]
  def getEmail(userEmail: String, status: String, emailID: String): Future[Seq[EmailInfoDTO]]
  def takeDraftMakeSent(userName: String, emailID: String): Future[Int]

}
