package api.controllers

import akka.actor.ActorSystem
import api.dto.CreateShareDTO
import api.validators.TokenValidator
import database.repository.{ ChatRepository, UserRepository }
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import slick.basic.DatabaseConfig
import slick.basic.DatabaseConfig
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

  implicit val userActions: UserRepository = new UserRepository(db)
  implicit val chatActions: ChatRepository = new ChatRepository(db)

  /**
   * Show inbox action
   * @return When a valid user is logged, the conversations are shown as an inbox
   */
  def inbox: Action[AnyContent] = tokenValidator.async { request =>
    request.userName.flatMap {
      chatActions.showInbox(_).map {
        inbox =>
          val chatsResult = JsObject(inbox.map(x => (x._1, JsString(x._2))))
          Ok(chatsResult)
      }
    }
  }

  /**
   *
   * @param chatID
   * @return
   */
  def getEmails(chatID: String): Action[AnyContent] = tokenValidator.async { request =>
    request.userName.flatMap {
      chatActions.getEmails(_, chatID).map {
        emails =>
          val emailsResult = JsObject(emails.map(x => (x._1, JsString(x._2))))
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
}
