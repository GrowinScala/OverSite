package api.controllers

import akka.actor.ActorSystem
import api.dtos.CreateShareDTO
import api.validators.TokenValidator
import database.repository.{ ChatRepository, UserRepository }
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Class injected with end-points
 */

class ChatController @Inject() (
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  db: Database)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  //TODO: You should "rethink" using local instances and replace them by injections ;)
  implicit val userActions: UserRepository = new UserRepository(db)
  implicit val chatActions: ChatRepository = new ChatRepository(db)

  /**
   * Get inbox action
   * @return When a valid user is logged, the conversations are shown as an inbox
   */
  def inbox: Action[AnyContent] = tokenValidator.async { request =>
    request.userName.flatMap {
      chatActions.getInbox(_).map {
        inbox =>
          val chatsResult = JsObject(inbox.map(x => (x._1, JsString(x._2))))
          Ok(chatsResult)
      }
    }
  }

  /**
   * @param chatID
   * @return
   */
  def getEmails(chatID: String): Action[AnyContent] = tokenValidator.async { request =>
    request.userName.flatMap {
      chatActions.getEmails(_, chatID).map {
        emails =>
          val emailsResult = JsArray(
            emails.map { x =>
              JsObject(Seq(
                ("Email ID:", JsString(x._1)),
                ("Header:", JsString(x._2))))
            })
          Ok(emailsResult)
      }
    }
  }

  def getEmail(chatID: String, emailID: String) = tokenValidator.async { request =>
    request.userName.flatMap {
      chatActions.getEmail(_, chatID, emailID).map {
        emails =>
          val emailsResult = JsArray(emails.map { x =>
            JsObject(
              //emailID, chatID, fromAddress, toAddress , header, body, dateOf
              Seq(
                ("Email ID:", JsString(emailID)),
                ("Chat ID:", JsString(chatID)),
                ("From address:", JsString(x._1)),
                ("To address:", JsString(x._2)),
                ("Header:", JsString(x._3)),
                ("Body", JsString(x._4)),
                ("Date:", JsString(x._5))))
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
        BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
      },
      share => {
        request.userName.map(
          chatActions.insertPermission(_, share))
        Future {
          Ok
        }
      })
  }

  def getShares: Action[AnyContent] = tokenValidator.async { request =>

    request.userName.flatMap(
      chatActions.getShares(_).map(
        emails => {
          val resultEmailID = JsObject(emails.map(x => (x._1, JsString(x._2))))
          Ok(resultEmailID)
        }))
  }

  /**
   *
   * @param shareID
   * @return
   */
  def getSharedEmails(shareID: String): Action[AnyContent] = tokenValidator.async { request =>

    request.userName.flatMap(
      chatActions.getSharedEmails(_, shareID).map(
        emails => {
          val resultEmailID = JsArray(
            emails.map { x =>
              JsObject(Seq(
                ("Email ID:", JsString(x._1)),
                ("Header:", JsString(x._2))))
            })
          Ok(resultEmailID)
        }))

  }

  def getSharedEmail(shareID: String, emailID: String): Action[AnyContent] = tokenValidator.async { request =>

    request.userName.flatMap(
      chatActions.getSharedEmail(_, shareID, emailID).map(
        email => {
          val resultEmailID = JsArray(
            email.map { x =>
              JsObject(Seq(
                ("Share ID:", JsString(shareID)),
                ("Email ID:", JsString(emailID)),
                ("Chat ID:", JsString(x._1)),
                ("From address:", JsString(x._2)),
                ("To address:", JsString(x._3)),
                ("Header:", JsString(x._4)),
                ("Body:", JsString(x._5)),
                ("Date:", JsString(x._6))))
            })
          Ok(resultEmailID)
        }))
  }

  def takePermissions: Action[JsValue] = tokenValidator(parse.json).async { request =>
    val shareResult = request.body.validate[CreateShareDTO]
    shareResult.fold(
      errors => Future {
        BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
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
