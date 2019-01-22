package database.repository

import api.dtos.CreateEmailDTO

import scala.concurrent.Future

trait DraftRepository {

  def updateDraft(draft: CreateEmailDTO, username: String, draftID: String): Future[String]
  def insertDraft(username: String, draft: CreateEmailDTO): Future[String]
  def takeDraftMakeSent(username: String, draftID: String, listCCs: Seq[String], listBCCs: Seq[String], listTos: Seq[String]): Future[String]
  def destinations(username: String, draftID: String): Future[(Seq[String], Seq[String], Seq[String])]
  def hasDestination(listTos: Seq[String], listBCCs: Seq[String], listCCs: Seq[String]): Future[Boolean]

}
