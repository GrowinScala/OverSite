package database.repository

import api.dtos.CreateEmailDTO

import scala.concurrent.Future

trait DraftRepository {

  def updateDraft(draft: CreateEmailDTO, username: String, draftID: String): Future[String]
  def insertDraft(username: String, draft: CreateEmailDTO): Future[String]
  //def takeDraftMakeSent(username: String, draftID : String): Future[Int]
}
