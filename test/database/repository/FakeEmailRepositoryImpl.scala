package database.repository

import api.dtos.{CreateEmailDTO, EmailInfoDTO, MinimalInfoDTO}
import definedStrings.testStrings.RepositoryStrings.EmptyString

import scala.concurrent.Future

class FakeEmailRepositoryImpl extends EmailRepository {

  def insertEmail(username: String, email: CreateEmailDTO): Future[String] ={
    Future.successful(EmptyString)
  }
  def getEmails(userEmail: String, status: String): Future[Seq[MinimalInfoDTO]]= {
    Future.successful(Seq(MinimalInfoDTO(EmptyString, EmptyString)))
  }

  def getEmail(userEmail: String, status: String, emailID: String): Future[Seq[EmailInfoDTO]]= {
    Future.successful(Seq(EmailInfoDTO(EmptyString,userEmail,Seq(EmptyString),EmptyString,EmptyString,EmptyString)))
  }

  def changeTrash(userName: String, emailID: String): Future[Int] = {
    Future.successful(0)
  }

}
