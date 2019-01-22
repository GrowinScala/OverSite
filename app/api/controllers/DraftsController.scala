package api.controllers

import akka.actor.ActorSystem
import api.JsonObjects.jsonErrors
import api.dtos.CreateEmailDTO
import api.validators.TokenValidator
import database.repository.{ ChatRepositoryImpl, DraftRepositoryImpl, UserRepositoryImpl }
import definedStrings.ApiStrings.MailSentStatus
import javax.inject.Inject
import play.api.libs.json.JsValue
import play.api.mvc.{ AbstractController, Action, ControllerComponents }
import slick.jdbc.MySQLProfile.api._

import scala.concurrent
import scala.concurrent.{ ExecutionContext, Future }

class DraftsController @Inject() (
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  implicit val db: Database,
  chatActions: ChatRepositoryImpl,
  usersActions: UserRepositoryImpl,
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