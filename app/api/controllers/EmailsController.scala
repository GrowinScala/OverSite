package api.controllers

import akka.actor.ActorSystem
import akka.stream.impl
import api.dto.CreateEmailDTO
import api.dto.EmailCreationDTO._
import database.repository.EmailRepository
import javax.inject._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor, Future }

@Singleton
class EmailsController @Inject() (cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  val emailActions = new EmailRepository("mysql")

  def index = Action.async {
    Future {
      Ok("something")
    }
  }

  def email(userName: String) = Action(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateEmailDTO]
    emailResult.fold(
      errors => Future.successful(BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))),
      email => emailActions.insertEmail(email).map(_ => Ok))
  }
}
