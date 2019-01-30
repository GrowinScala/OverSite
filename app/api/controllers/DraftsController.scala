package api.controllers

import akka.actor.ActorSystem
import api.JsonObjects.jsonErrors
import api.dtos.{ CreateEmailDTO, DraftStatusDTO, MinimalInfoDTO }
import api.validators.TokenValidator
import database.repository.EmailRepository
import definedStrings.ApiStrings.{ MailSentStatus, _ }
import javax.inject.{ Inject, Singleton }
import play.api.libs.json.{ JsArray, JsValue, Json }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton class DraftsController @Inject() (
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  emailActions: EmailRepository)(implicit exec: ExecutionContext) extends AbstractController(cc) {

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
          emailActions.insertDraft(_, draft))
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

    request.userName.flatMap(
      emailActions.getDrafts(_, isTrash.getOrElse(false)).map {
        drafts =>
          val result = drafts.map(draft =>
            MinimalInfoDTO.addLink(
              draft,
              if (isTrash.getOrElse(false))
                List(routes.DraftsController.getDraft(draft.Id, isTrash).absoluteURL())
              else List(routes.DraftsController.getDraft(draft.Id, Option(false)).absoluteURL())))
          Ok(Json.toJson(result))
      })
  }

  /**
   * Selects an email after filtering through isTrash and draftID
   * @param isTrash Identification of the email status
   * @param draftID Identification of the email
   * @return Action that shows the emailID required
   */
  def getDraft(draftID: String, isTrash: Option[Boolean]): Action[AnyContent] = tokenValidator.async { request =>

    implicit val req: RequestHeader = request

    request.userName.flatMap(
      emailActions.getDraft(_, draftID, isTrash.getOrElse(false)).map(
        drafts => {
          val resultDraftID = JsArray(drafts.map(Json.toJson(_)))
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
        request.userName.flatMap(emailActions.updateDraft(_, draftID, draft))
        Future.successful {
          Ok(EmailUpdated)
        }
      })

  }

  /**
   * Receive a target draft email and sends it if that email has a destination parameter
   * @param draftID Identification of the email
   */
  def toSentOrDraft(draftID: String): Action[JsValue] = tokenValidator(parse.json).async { request =>

    val draftStatusResult = request.body.validate[DraftStatusDTO]

    draftStatusResult.fold(
      errors => {
        Future {
          BadRequest(jsonErrors(errors))
        }
      },
      draft => draft.status match {

        case StatusSend => request.userName.flatMap(username =>
          emailActions.destinations(username, draftID).flatMap {
            case (listTos, listBCCs, listCCs) => emailActions.hasDestination(listTos, listBCCs, listCCs).map(
              bool =>
                if (bool) {
                  emailActions.takeDraftMakeSent(username, draftID, listTos, listBCCs, listCCs)
                  Ok(MailSentStatus)
                } else
                  BadRequest(ImpossibleToSendDraft))
          })

        case StatusTrash =>
          request.userName.map(
            emailActions.moveInOutTrash(_, draftID, trash = true))
          Future.successful {
            Ok(EmailUpdated)
          }

        case StatusDraft =>
          request.userName.map(
            emailActions.moveInOutTrash(_, draftID, trash = false))
          Future.successful {
            Ok(EmailUpdated)
          }

        case _ => Future.successful {
          BadRequest(ImpossibleStatusDraft)
        }
      })
  }
}