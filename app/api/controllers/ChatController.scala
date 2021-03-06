package api.controllers

import akka.actor.ActorSystem
import api.JsonObjects.jsonErrors
import api.dtos.{ CreateShareDTO, MinimalInfoDTO, TrashInfoDTO }
import api.validators.TokenValidator
import database.repository.{ ChatRepository, UserRepository }
import definedStrings.ApiStrings._
import javax.inject._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/** Class injected with end-points*/

@Singleton class ChatController @Inject() (
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  chatActions: ChatRepository,
  usersActions: UserRepository)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  /**
   * Get inbox action
   * @return When a valid user is logged, the conversations are shown as an inbox
   */
  def inbox(isTrash: Option[Boolean]): Action[AnyContent] = tokenValidator.async { request =>

    implicit val req: RequestHeader = request

    request.userName.flatMap {
      chatActions.getInbox(_, isTrash.getOrElse(false)).map {
        emails =>
          val result = emails.map(email =>
            MinimalInfoDTO.addLink(
              email,
              List(routes.ChatController.getEmails(email.Id, isTrash).absoluteURL())))
          Ok(Json.toJson(result))
      }
    }
  }

  /**
   * Selects the emails of a selected chatID
   * @param chatID Identification of the chat
   * @return Action that shows the EmailID and respective Header of all emails that belong to the chat selected
   */

  def getEmails(chatID: String, isTrash: Option[Boolean]): Action[AnyContent] = tokenValidator.async { request =>
    implicit val req: RequestHeader = request

    request.userName.flatMap {
      chatActions.getEmails(_, chatID, isTrash.getOrElse(false)).map {
        emails =>
          val result = emails.map(email =>

            MinimalInfoDTO.addLink(
              email,
              if (isTrash.getOrElse(false))
                List(routes.EmailsController.getEmail(email.Id, Option(IsTrashString)).absoluteURL())
              else List(routes.EmailsController.getEmail(email.Id, Option(EmptyString)).absoluteURL())))
          Ok(Json.toJson(result))
      }
    }
  }

  /**
   * Function that moves all the mails from a certain chatID to trash or vice versa
   * @param chatID Identification of the chat
   * @return Action that update the trash boolean status of each emailID involved
   */
  def moveInOutTrash(chatID: String): Action[JsValue] = tokenValidator(parse.json).async { request =>

    val toTrashResult = request.body.validate[TrashInfoDTO]

    toTrashResult.fold(

      errors => Future.successful { BadRequest(jsonErrors(errors)) },
      move => {
        request.userName.flatMap(chatActions.changeTrash(_, chatID, move.toTrash))
        Future.successful { Ok }
      })
  }

  /**
   * Give permission to oversight a personal conversation
   * @return Permission insertion to an userID, from another user to oversight an specific chat
   */
  def supervised: Action[JsValue] = tokenValidator(parse.json).async { request =>
    val emailResult = request.body.validate[CreateShareDTO]

    emailResult.fold(

      errors => Future.successful { BadRequest(jsonErrors(errors)) },
      share => {
        request.userName.map(chatActions.insertPermission(_, share))
        Future.successful { Ok }
      })
  }

  /**
   * Get the header and emailID
   * @return List of email IDs and respective headers of the shared chats from the user
   */
  def getShares: Action[AnyContent] = tokenValidator.async { request =>

    request.userName.flatMap(
      chatActions.getShares(_).map(
        emails => Ok(Json.toJson(emails))))
  }

  /**
   * Get the list of Emails of a exact shareID
   * @param shareID Identification of the share
   * @return List of email IDs and respective headers of the shared chat required
   */
  def getSharedEmails(shareID: String): Action[AnyContent] = tokenValidator.async { request =>

    implicit val req: RequestHeader = request

    request.userName.flatMap(
      chatActions.getSharedEmails(_, shareID).map(
        emails => {
          val result = emails.map(
            email => MinimalInfoDTO.addLink(email, List(routes.EmailsController.getEmail(email.Id, Option("")).absoluteURL())))

          Ok(Json.toJson(result))
        }))
  }

  /**
   * Delete permission from database
   * @return delete the permission from share Table
   */
  def takePermissions: Action[JsValue] = tokenValidator(parse.json).async { request =>
    val shareResult = request.body.validate[CreateShareDTO]

    shareResult.fold(

      errors => Future.successful { BadRequest(jsonErrors(errors)) },
      share => {
        request.userName.map(chatActions.deletePermission(_, share.supervisor, share.chatID))
        Future.successful { Ok }
      })
  }

}
