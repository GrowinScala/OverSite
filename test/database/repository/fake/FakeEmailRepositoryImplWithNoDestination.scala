
package database.repository.fake

import api.dtos.{ CreateEmailDTO, EmailInfoDTO, MinimalInfoDTO, _ }
import database.repository.EmailRepository
import definedStrings.testStrings.RepositoryStrings.EmptyString

import scala.concurrent.Future

class FakeEmailRepositoryImplWithNoDestination extends EmailRepository {

  def insertEmail(username: String, email: CreateEmailDTO): Future[String] = {
    Future.successful(EmptyString)
  }
  def getEmails(userEmail: String, status: String): Future[Seq[MinimalInfoDTO]] = {
    Future.successful(Seq(MinimalInfoDTO(EmptyString, EmptyString)))
  }

  def getEmail(userEmail: String, status: String, emailID: String): Future[EmailInfoDTO] = {
    Future.successful(EmailInfoDTO(EmptyString, userEmail, Seq(EmptyString), EmptyString, EmptyString, EmptyString))
  }

  def changeTrash(userName: String, emailID: String, moveToTrash: Boolean): Future[Int] = {
    Future.successful(0)
  }

  def insertDraft(username: String, draft: CreateEmailDTO): Future[String] = {
    Future.successful("")
  }

  def getDrafts(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]] = {

    Future.successful(Seq(MinimalInfoDTO(EmptyString, EmptyString)))
  }

  def updateDraft(username: String, draftID: String, draft: CreateEmailDTO): Future[String] = {
    Future.successful(EmptyString)
  }

  def getDraft(userEmail: String, draftID: String, isTrash: Boolean): Future[DraftInfoDTO] = {
    Future.successful(DraftInfoDTO(EmptyString, userEmail, Seq(EmptyString), Seq(EmptyString), Seq(EmptyString), EmptyString, EmptyString, EmptyString))
  }

  def takeDraftMakeSent(username: String, draftID: String, listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[String] = {
    Future.successful(EmptyString)
  }

  def destinations(username: String, draftID: String): Future[(Seq[String], Seq[String], Seq[String])] = {
    Future.successful((Seq(), Seq(), Seq()))
  }

  def hasDestination(listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[Boolean] = {
    Future.successful { false }
  }

  def moveInOutTrash(userEmail: String, draftID: String, trash: Boolean): Future[Int] = {
    Future.successful(0)
  }

  def getSharedEmail(userEmail: String, shareID: String, emailID: String): Future[EmailInfoDTO] = {
    Future.successful(EmailInfoDTO(EmptyString, userEmail, Seq(EmptyString), EmptyString, EmptyString, EmptyString))
  }
}
