package database.repository

import api.dtos.{ CreateEmailDTO, DraftInfoDTO, EmailInfoDTO, MinimalInfoDTO }

import scala.concurrent.Future

trait EmailRepository {

  def insertEmail(username: String, email: CreateEmailDTO): Future[String]
  def changeTrash(userName: String, emailID: String, moveToTrash: Boolean): Future[Int]
  def getEmails(userEmail: String, status: String): Future[Seq[MinimalInfoDTO]]
  def getEmail(userEmail: String, status: String, emailID: String): Future[EmailInfoDTO]
  def insertDraft(username: String, draft: CreateEmailDTO): Future[String]
  def getDrafts(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]]
  def updateDraft(username: String, draftID: String, draft: CreateEmailDTO): Future[String]
  def getDraft(userEmail: String, draftID: String, isTrash: Boolean): Future[Seq[DraftInfoDTO]]
  def takeDraftMakeSent(username: String, draftID: String, listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[String]
  def destinations(username: String, draftID: String): Future[(Seq[String], Seq[String], Seq[String])]
  def hasDestination(listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[Boolean]
  def moveInOutTrash(userEmail: String, draftID: String, trash: Boolean): Future[Int]
  def getSharedEmail(userEmail: String, shareID: String, emailID: String): Future[EmailInfoDTO]
}
