package database.repository

import api.dtos.{ CreateEmailDTO, DraftInfoDTO, EmailInfoDTO, MinimalInfoDTO }

import scala.concurrent.Future

trait EmailRepository {

  def insertEmail(username: String, email: CreateEmailDTO): Future[String]
  def getEmails(userEmail: String, status: String): Future[Seq[MinimalInfoDTO]]
  def getEmail(userEmail: String, status: String, emailID: String): Future[Seq[EmailInfoDTO]]
  def changeTrash(userName: String, emailID: String): Future[Int]
  def insertDraft(username: String, draft: CreateEmailDTO): Future[String]
  def getDrafts(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]]
  def updateDraft(draft: CreateEmailDTO, username: String, draftID: String): Future[String]
  def getDraft(userEmail: String, isTrash: Boolean, draftID: String): Future[Seq[DraftInfoDTO]]
  def takeDraftMakeSent(username: String, draftID: String, listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[String]
  def destinations(username: String, draftID: String): Future[(Seq[String], Seq[String], Seq[String])]
  def hasDestination(listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[Boolean]
  def moveInOutTrash(userEmail: String, draftID: String, trash: Boolean): Future[Int]

}
