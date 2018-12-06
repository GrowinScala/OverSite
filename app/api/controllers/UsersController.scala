package api.controllers

import akka.actor.ActorSystem
import api.dto.UserCreationDTO.CreateUserDTO
import database.repository.UserRepository
import javax.inject._
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class UsersController @Inject() (cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {
  val userActions = new UserRepository("mysql")

  def signin = Action(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateUserDTO]
    Future {
      emailResult.fold(
        errors => {
          BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
        },
        user => {
          userActions.insertUser(user)
          Ok("User created")
        }
      )
    }
  }

  def login = Action(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateUserDTO]
    Future {
      emailResult.fold(
        errors => {
          BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
        },
        user => {
          val x = userActions.loginUser(user)
          x.map(_.length).onComplete(_.getOrElse() match {
            case 1 => {
              userActions.insertLogin(user)
              Ok
            }
            case _ => Forbidden("Username or password doesn´t match")
          })
          Ok
        }
      )
    }
  }
}
