package database.repository

import api.dtos.{CreateEmailDTO, EmailInfoDTO}

import scala.concurrent.Future

trait EmailRepository {

  def insertEmail(username: String, email: CreateEmailDTO): Future[String]
  def getEmails(userEmail: String, status: String): Future[Seq[(String, String)]]
  def getEmail(userEmail: String, status: String, emailID: String): Future[Seq[EmailInfoDTO]]
  def takeDraftMakeSent(userName: String, emailID: String): Future[Int]

}
