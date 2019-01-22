package database.repository

import api.dtos.{ CreateEmailDTO, DraftInfoDTO, MinimalInfoDTO }

import scala.concurrent.Future

trait DraftRepository {

  def updateDraft(draft: CreateEmailDTO, username: String, draftID: String): Future[String]
  def insertDraft(username: String, draft: CreateEmailDTO): Future[String]
  def getDrafts(userEmail: String, isTrash: Boolean): Future[Seq[MinimalInfoDTO]]
  def getDraft(userEmail: String, isTrash: Boolean, draftID: String): Future[Seq[DraftInfoDTO]]

}
