package database.repository.fake

import api.dtos._
import database.repository.ChatRepository
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

  def changeTrash(username: String, chatID: String, moveToTrash: Boolean): Future[Int] = {
    Future.successful(0)
  }

  def insertPermission(from: String, share: CreateShareDTO): Future[String] = {
    Future.successful(EmptyString)
  }

  def getShares(userEmail: String): Future[Seq[MinimalShareInfoDTO]] = {
    Future.successful(Seq(MinimalShareInfoDTO(EmptyString, EmptyString, EmptyString)))
  }

  def getSharedEmails(userEmail: String, shareID: String): Future[Seq[MinimalInfoDTO]] = {
    Future.successful(Seq(MinimalInfoDTO(EmptyString, EmptyString)))
  }

  def deletePermission(from: String, to: String, chatID: String): Future[Int] = {
    Future.successful(0)
  }

}
