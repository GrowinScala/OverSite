package api.controllers

import akka.actor.ActorSystem
import api.JsonObjects.jsonErrors
import api.dtos.{CreateEmailDTO, MinimalInfoDTO}
import api.validators.TokenValidator
import database.repository.DraftRepositoryImpl
import definedStrings.ApiStrings.MailSentStatus
import javax.inject.Inject
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class DraftsController @Inject() (
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  implicit val db: Database,
  draftActions: DraftRepositoryImpl)(implicit exec: ExecutionContext) extends AbstractController(cc) {

  /**
   * Aims to send an email from an user to an userID
   * @return inserts the email information to the database
   */
  def draft: Action[JsValue] = tokenValidator(parse.json).async { request =>
    val draftResult = request.body.validate[CreateEmailDTO]

    draftResult.fold(
      errors => {
        Future {
          BadRequest(jsonErrors(errors))
        }
      },
      draft => {
        request.userName.map(
          draftActions.insertDraft(_, draft))
        Future.successful {
          Ok(MailSentStatus)
        }
      })
  }

  /**
   * Selects the drafts of an user
   * @param isTrash Optional boolean
   * @return Action that shows the DraftID and respective Header of all drafts
   */

  def getDrafts(isTrash: Option[Boolean]): Action[AnyContent] = tokenValidator.async { request =>
    implicit val req: RequestHeader = request

    request.userName.flatMap {
      draftActions.getDrafts(_, isTrash.getOrElse(false)).map {
        drafts =>
          val result = drafts.map(draft =>

            MinimalInfoDTO.addLink(
              draft,
              //if (isTrash.getOrElse(false))
              //List(routes.EmailsController.getEmail(draft.Id, Option("isTrash")).absoluteURL())
              //else List(routes.EmailsController.getEmail(draft.Id, Option("")).absoluteURL())))
              List("")))
          Ok(Json.toJson(result))
      }
    }
  }

  /**
   * Selects an email after filtering through status and emailID
   * @param status Identification of the email status
   * @param draftID Identification of the email
   * @return Action that shows the emailID required
   */
  def getDraft(draftID: String, isTrash: Option[Boolean]): Action[AnyContent] = tokenValidator.async { request =>

    implicit val req: RequestHeader = request

    request.userName.flatMap(
      draftActions.getDraft(_, isTrash.getOrElse(false), draftID).map(
        drafts => {
          val resultDraftID = JsArray(
            drafts.map { draft =>
              Json.toJson(draft)
            })
          Ok(resultDraftID)
        }))
  }

  def updateDraft(draftID: String): Action[JsValue] = tokenValidator(parse.json).async { request =>

    val draftResult = request.body.validate[CreateEmailDTO]

    draftResult.fold(
      errors => {
        Future {
          BadRequest(jsonErrors(errors))
        }
      },
      draft => {
        request.userName.map(
          draftActions.updateDraft(draft, _, draftID))
        Future.successful {
          Ok("Email updated")
        }
      })
  }

}