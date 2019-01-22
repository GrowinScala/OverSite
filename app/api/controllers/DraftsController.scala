package api.controllers

import akka.actor.ActorSystem
import api.JsonObjects.jsonErrors
import api.dtos.{CreateEmailDTO, EmailMinimalInfoDTO}
import api.validators.TokenValidator
import database.repository.{ChatRepositoryImpl, DraftRepositoryImpl, UserRepositoryImpl}
import definedStrings.ApiStrings.MailSentStatus
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent
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
          Ok("Draft sent")
        }
      })
  }

  /**
    * Selects the drafts of an user
    * @param isTrash Optional
    * @return Action that shows the EmailID and respective Header of all emails that belong to the chat selected
    */

  def getDrafts(isTrash: Option[Boolean]): Action[AnyContent] = tokenValidator.async { request =>
    implicit val req: RequestHeader = request

    request.userName.flatMap {
      draftActions.getDrafts(_, isTrash.getOrElse(false)).map {
        drafts =>
          val result = drafts.map(draft =>

            EmailMinimalInfoDTO.addLink(
              draft,
              //if (isTrash.getOrElse(false))
              //List(routes.EmailsController.getEmail(draft.Id, Option("isTrash")).absoluteURL())
              //else List(routes.EmailsController.getEmail(draft.Id, Option("")).absoluteURL())))
              List("")))
          Ok(Json.toJson(result))
      }
    }
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