package api.controllers

import akka.actor.ActorSystem
import database.mappings.EmailObject._
import dto.EmailCreationDTO._
import javax.inject._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class EmailsController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem) (implicit exec: ExecutionContext)
  extends AbstractController(cc) {


  def index =  Action{
    Ok("something")
  }


  def email(userName: String) = Action(parse.json) { request: Request[JsValue]  =>
    val emailResult = request.body.validate[CreateEmailDTO]
    emailResult.fold(
      errors => {
        BadRequest("Bad Request")
      },
      email => {
    execDB(insertEmail(email))
    Ok
      }
    )
  }
}
