package database.repository.fake

import api.dtos.{ CreateEmailDTO, DraftInfoDTO, MinimalInfoDTO }
import database.repository.DraftRepository
import definedStrings.testStrings.RepositoryStrings.EmptyString

import scala.concurrent.Future

class FakeDraftRepositoryImpl extends DraftRepository {

  def insertDraft(username: String, draft: CreateEmailDTO): Future[String] = {
    Future.successful("")
  }

  def getDrafts(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]] = {

    Future.successful(Seq(MinimalInfoDTO(EmptyString, EmptyString)))
  }

  def updateDraft(draft: CreateEmailDTO, username: String, draftID: String): Future[String] = {
    Future.successful(EmptyString)
  }

  def getDraft(userEmail: String, isTrash: Boolean, draftID: String): Future[Seq[DraftInfoDTO]] = {
    Future.successful(Seq(DraftInfoDTO(EmptyString, userEmail, Seq(EmptyString), Seq(EmptyString), Seq(EmptyString), EmptyString, EmptyString, EmptyString)))
  }

  def takeDraftMakeSent(username: String, draftID: String, listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[String] = {
    Future.successful(EmptyString)
  }

  def destinations(username: String, draftID: String): Future[(Seq[String], Seq[String], Seq[String])] = {
    Future.successful((Seq(EmptyString), Seq(EmptyString), Seq(EmptyString)))
  }

  //TODO when testing !hasDestination to test the case where there are no destinations
  def hasDestination(listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[Boolean] = {
    Future.successful { true }
  }

  def moveInOutTrash(userEmail: String, draftID: String, trash: Boolean): Future[Int] = {
    Future.successful(0)
  }

}
