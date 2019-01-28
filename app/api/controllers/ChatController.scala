package api.controllers

import akka.actor.ActorSystem
import api.JsonObjects.jsonErrors
import api.dtos.AuxFunctions._
import api.dtos.{ CreateShareDTO, MinimalInfoDTO, TrashInfoDTO }
import api.validators.TokenValidator
import database.properties.DBProperties
import database.repository.{ ChatRepository, ChatRepositoryImpl, UserRepository, UserRepositoryImpl }
import database.repository.{ ChatRepositoryImpl, UserRepositoryImpl }
import definedStrings.ApiStrings._
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._
import definedStrings.ApiStrings._
import definedStrings.DatabaseStrings.OversiteDB

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
   * Selects an email after filtering through chatID and emailID
   * @param chatID Identification of the chat
   * @param emailID Identification of the email
   * @return Action that shows the emailID required
   */
  def getEmail(chatID: String, emailID: String, isTrash: Option[Boolean]): Action[AnyContent] = tokenValidator.async { request =>
    request.userName.flatMap {
      chatActions.getEmail(_, chatID, emailID, isTrash.getOrElse(false)).map {
        emails =>
          val emailsResult = JsArray(
            emails.map { email =>
              Json.toJson(convertEmailInfoToSender(email, emailID))
            })
          Ok(emailsResult)
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
      errors => {
        println(request.body.toString())
        Future {
          BadRequest(jsonErrors(errors))
        }
      },
      move => {
        request.userName.flatMap(
          chatActions.changeTrash(_, chatID, move.toTrash))
        Future.successful {
          Ok
        }
      })
  }

  /**
   * Give permission to oversight a personal conversation
   * @return Permission insertion to an userID, from another user to oversight an specific chat
   */
  def supervised: Action[JsValue] = tokenValidator(parse.json).async { request =>
    val emailResult = request.body.validate[CreateShareDTO]
    emailResult.fold(
      errors => Future {
        BadRequest(jsonErrors(errors))
      },
      share => {
        request.userName.map(
          chatActions.insertPermission(_, share))
        Future {
          Ok
        }
      })
  }

  /**
   * Get the header and emailID
   * @return List of email IDs and respective headers of the shared chats from the user
   */
  def getShares: Action[AnyContent] = tokenValidator.async { request =>

    request.userName.flatMap(
      chatActions.getShares(_).map(
        emails => {
          Ok(Json.toJson(emails))
        }))
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

          val result = emails.map(email =>
            MinimalInfoDTO.addLink(
              email,
              List(routes.EmailsController.getEmail(email.Id, Option("")).absoluteURL())))
          Ok(Json.toJson(result))
        }))

  }

  /**
   * Get the email that corresponds to the shareID and emailID inserted
   * @param shareID Identification of the share
   * @param emailID Identification of the email
   * @return All details of the email required
   */

  def getSharedEmail(shareID: String, emailID: String): Action[AnyContent] = tokenValidator.async { request =>

    request.userName.flatMap(
      chatActions.getSharedEmail(_, shareID, emailID).map(
        emails => {
          val resultEmailID = JsArray(
            emails.map { email =>
              Json.toJson(convertEmailInfoToShareSender(email, shareID, emailID))
            })
          Ok(resultEmailID)
        }))
  }

  /**
   * Delete permission from database
   * @return delete the permission from share Table
   */
  def takePermissions: Action[JsValue] = tokenValidator(parse.json).async { request =>
    val shareResult = request.body.validate[CreateShareDTO]
    shareResult.fold(
      errors => Future {
        BadRequest(jsonErrors(errors))
      },
      share => {
        request.userName.map(
          chatActions.deletePermission(_, share.supervisor, share.chatID))
        Future.successful {
          Ok
        }
      })
  }

}
