package database.repository

import api.dtos.{ CreateEmailDTO, CreateShareDTO, EmailInfoDTO, MinimalInfoDTO }
import definedStrings.testStrings.RepositoryStrings.EmptyString

import scala.concurrent.Future

class FakeChatRepositoryImpl extends ChatRepository {

  def insertChat(email: CreateEmailDTO, chatID: String): Future[String] = {
    Future.successful(EmptyString)
  }

  def getInbox(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]] = {
    Future.successful(Seq(MinimalInfoDTO(EmptyString, EmptyString)))
  }

  def getEmails(userEmail: String, chatID: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]] = {
    Future.successful(Seq(MinimalInfoDTO(EmptyString, EmptyString)))
  }
  def getEmail(userEmail: String, chatID: String, emailID: String, isTrash: Boolean): Future[Seq[EmailInfoDTO]] = {
    Future.successful(Seq(EmailInfoDTO(EmptyString, userEmail, Seq(EmptyString), EmptyString, EmptyString, EmptyString)))
  }

  def insertPermission(from: String, share: CreateShareDTO): Future[String] = {
    Future.successful(EmptyString)
  }

  def getShares(userEmail: String): Future[Seq[MinimalInfoDTO]] = {
    Future.successful(Seq(MinimalInfoDTO(EmptyString, EmptyString)))
  }

  def getSharedEmails(userEmail: String, shareID: String): Future[Seq[MinimalInfoDTO]] = {
    Future.successful(Seq(MinimalInfoDTO(EmptyString, EmptyString)))
  }

  def getSharedEmail(userEmail: String, shareID: String, emailID: String): Future[Seq[EmailInfoDTO]] = {
    Future.successful(Seq(EmailInfoDTO(EmptyString, userEmail, Seq(EmptyString), EmptyString, EmptyString, EmptyString)))
  }

  def deletePermission(from: String, to: String, chatID: String): Future[Int] = {
    Future.successful(0)
  }

}
