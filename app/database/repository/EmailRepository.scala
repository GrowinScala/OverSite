package database.repository

import api.dtos.{ CreateEmailDTO, EmailInfoDTO, MinimalInfoDTO }

import scala.concurrent.Future

trait EmailRepository {

  def insertEmail(username: String, email: CreateEmailDTO): Future[String]
  def getEmails(userEmail: String, status: String): Future[Seq[MinimalInfoDTO]]
  def getEmail(userEmail: String, status: String, emailID: String): Future[Seq[EmailInfoDTO]]
  def changeTrash(userName: String, emailID: String): Future[Int]

}
