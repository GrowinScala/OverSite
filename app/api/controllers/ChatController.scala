package api.controllers

import akka.actor.ActorSystem
import api.dtos.CreateShareDTO
import api.validators.TokenValidator
import database.repository.{ ChatRepository, ChatRepositoryImpl, UserRepositoryImpl }
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._
import definedStrings.ApiStrings._

import scala.concurrent.{ ExecutionContext, Future }

/** Class injected with end-points*/

class ChatController @Inject() (
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  implicit val db: Database,
  chatActions: ChatRepositoryImpl,
  usersActions: UserRepositoryImpl)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  /**
   * Get inbox action
   * @return When a valid user is logged, the conversations are shown as an inbox
   */
  def inbox: Action[AnyContent] = tokenValidator.async { request =>
    request.userName.flatMap {
      chatActions.getInbox(_).map {
        inbox =>
          val chatsResult = JsArray(
            inbox.map { x =>
              JsObject(Seq(
                (EmailIDJSONField, JsString(x._1)),
                (HeaderJSONField, JsString(x._2))))
            })
          Ok(chatsResult)
      }
    }
  }

  /**
   * Selects the emails of a selected chatID
   * @param chatID Identification of the chat
   * @return Action that shows the EmailID and respective Header of all emails that belong to the chat selected
   */
  def getEmails(chatID: String): Action[AnyContent] = tokenValidator.async { request =>
    request.userName.flatMap {
      chatActions.getEmails(_, chatID).map {
        emails =>
          val emailsResult = JsArray(
            emails.map { x =>
              JsObject(Seq(
                (EmailIDJSONField, JsString(x._1)),
                (HeaderJSONField, JsString(x._2))))
            })
          Ok(emailsResult)
      }
    }
  }

  /**
   * Selects an email after filtering through chatID and emailID
   * @param chatID Identification of the chat
   * @param emailID Identification of the email
   * @return Action that shows the emailID required
   */
  def getEmail(chatID: String, emailID: String) = tokenValidator.async { request =>
    request.userName.flatMap {
      chatActions.getEmail(_, chatID, emailID).map {
        emails =>
          val emailsResult = JsArray(emails.map { x =>
            JsObject(
              //emailID, chatID, fromAddress, toAddress , header, body, dateOf
              Seq(
                (EmailIDJSONField, JsString(emailID)),
                (ChatIDJSONField, JsString(chatID)),
                (FromAddressJSONField, JsString(x._1)),
                (ToAddressJSONField, JsString(x._2)),
                (HeaderJSONField, JsString(x._3)),
                (BodyJSONField, JsString(x._4)),
                (DateJSONField, JsString(x._5))))
          })
          Ok(emailsResult)
      }
    }
  }

  /**
   * Give permission to oversight a personal conversation
   * @return Permission insertion to an userID, from another user to oversight an specific chat
   */
  def supervised: Action[JsValue] = tokenValidator(parse.json).async { request =>
    val emailResult = request.body.validate[CreateShareDTO]
    emailResult.fold(
      errors => Future {
        BadRequest(Json.obj(StatusJSONField -> ErrorString, MessageString -> JsError.toJson(errors)))
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
          val resultEmailID = JsArray(
            emails.map { x =>
              JsObject(Seq(
                (EmailIDJSONField, JsString(x._1)),
                (HeaderJSONField, JsString(x._2))))
            })
          Ok(resultEmailID)
        }))
  }

  /**
   * Get the list of Emails of a exact shareID
   * @param shareID Identification of the share
   * @return List of email IDs and respective headers of the shared chat required
   */
  def getSharedEmails(shareID: String): Action[AnyContent] = tokenValidator.async { request =>

    request.userName.flatMap(
      chatActions.getSharedEmails(_, shareID).map(
        emails => {
          val resultEmailID = JsArray(
            emails.map { x =>
              JsObject(Seq(
                (EmailIDJSONField, JsString(x._1)),
                (HeaderJSONField, JsString(x._2))))
            })
          Ok(resultEmailID)
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
        email => {
          val resultEmailID = JsArray(
            email.map { x =>
              JsObject(Seq(
                (ShareIDJSONField, JsString(shareID)),
                (EmailIDJSONField, JsString(emailID)),
                (ChatIDJSONField, JsString(x._1)),
                (FromAddressJSONField, JsString(x._2)),
                (ToAddressJSONField, JsString(x._3)),
                (HeaderJSONField, JsString(x._4)),
                (BodyJSONField, JsString(x._5)),
                (DateJSONField, JsString(x._6))))
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
        BadRequest(Json.obj(StatusJSONField -> ErrorString, MessageString -> JsError.toJson(errors)))
      },
      share => {
        request.userName.map(
          chatActions.deletePermission(_, share.supervisor, share.chatID))
        Future {
          Ok
        }
      })
  }

}
