package api.controllers

import akka.actor.ActorSystem
import api.dto.EmailCreationDTO._
import database.repository.EmailRepository
import javax.inject._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class EmailsController @Inject() (cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {
  val emailActions = new EmailRepository()

  def index = Action.async {
    Future {
      Ok("something")
    }
  }

  def email(userName: String) = Action(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateEmailDTO]
    Future {
      emailResult.fold(
        errors => {
          BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
        },
        email => {
          emailActions.insertEmail(email)
          Ok("Email received")
        })
    }
  }
}
