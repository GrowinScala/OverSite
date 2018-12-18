package api.controllers

import akka.actor.ActorSystem
import api.dto.{ CreateEmailProfileDTO, CreateShareDTO, CreateUserDTO }
import api.validators.TokenValidator
import database.repository.{ ChatRepository, UserRepository }
import javax.inject._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Class that is injected with end-points
 * @param cc
 * @param actorSystem
 * @param exec
 */
@Singleton
class ChatController @Inject() (tokenValidator: TokenValidator, cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {
  val userActions = new UserRepository("mysql")
  val chatActions = new ChatRepository("mysql")

  /**
   * Show inbox action
   * @return When a valid user is inserted, it is added in the database, otherwise an error message is sent
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

  def supervised = tokenValidator(parse.json).async { request =>
    val emailResult = request.body.validate[CreateShareDTO]
    // TEMPORARILY VALUE
    val userName: String = "pppluis@cmail.com"
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
