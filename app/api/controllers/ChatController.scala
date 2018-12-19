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

  implicit val userActions = new UserRepository(db)
  implicit val chatActions = new ChatRepository(db)

  /**
   * Show inbox action
   * @return When a valid user is logged, the conversations are shown as an inbox
   */
  def inbox = tokenValidator.async { request =>
    request.userName.flatMap {
      chatActions.showInbox(_).map {
        inbox =>
          val chatsResult = JsObject(inbox.map(x => (x._1, JsString(x._2))))
          Ok(chatsResult)
      }
    }
  }

  /**
   * Give permission to oversight a personal conversation
   * @return Permission insertion to an userID, from another user to oversight an specific chat
   */
  def supervised = tokenValidator(parse.json).async { request =>
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
