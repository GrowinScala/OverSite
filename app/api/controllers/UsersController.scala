package api.controllers

import akka.actor.ActorSystem
import api.dto.CreateUserDTO
import api.dto.UserCreationDTO._
import database.repository.UserRepository
import javax.inject._
import play.api.libs.json.{ JsError, JsValue, Json }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

class UsersController @Inject() (cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {
  val userActions = new UserRepository("mysql")

  def signin = Action(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateUserDTO]
    emailResult.fold(
      errors => Future.successful(BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))),
      user => userActions.insertUser(user).map(_ => Ok))
  }
}

