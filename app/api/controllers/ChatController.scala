package api.controllers

import akka.actor.ActorSystem
import api.dto.CreateUserDTO
import database.repository.UserRepository
import javax.inject._
import play.api.libs.json.{ JsError, JsValue, Json }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Class that is injected with end-points
 * @param cc
 * @param actorSystem
 * @param exec
 */
@Singleton
class ChatController @Inject() (cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {
  val userActions = new UserRepository("mysql")

  /**
   * Sign in action
   * @return When a valid user is inserted, it is added in the database, otherwise an error message is sent
   */
  def inbox(userName: String) = Action(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateUserDTO]

    val authToken = request.headers.get("Token").getOrElse("")

    emailResult.fold(
      errors => {
        Future { BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors))) }
      },
      user => {
        userActions.insertUser(user)
        Future { Ok("User created") }
      })
  }

}
