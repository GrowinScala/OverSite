package api.controllers

import akka.actor.ActorSystem
import api.dto.EmailCreationDTO._
import database.repository.EmailRepository
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.json.{ JsError, JsValue, Json }

import scala.concurrent.ExecutionContext

@Singleton
class EmailsController @Inject() (cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {
  val emailActions = new EmailRepository()

  def index = Action {
    Ok("something")
  }

  //TODO: Use async, implement implicit writable to show errors to client.
  def email(userName: String) = Action(parse.json) { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateEmailDTO]
    emailResult.fold(
      errors => {
        BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
      },
      email => {
        emailActions.insertEmail(email)
        Ok
      })
  }
}
