package api.controllers

import akka.actor.ActorSystem
import api.dto.CreateEmailDTO
import database.repository.{ EmailRepository, UserRepository }
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
class EmailsController @Inject() (cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  val emailActions = new EmailRepository("mysql")
  val usersActions = new UserRepository("mysql")

  def index = Action.async {
    Future {
      Ok("something")
    }
  }

  /**
   *
   * @param userName
   * @return
   */
  def email(userName: String) = Action(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateEmailDTO]
    val authToken = request.headers.get("Token").getOrElse("")

    emailResult.fold(
      errors => { Future { BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors))) } },
      email => {
        usersActions.validateToken(userName, authToken).map(_.length).map({
          case 1 =>
            emailActions.insertEmail(email)
            Ok("Mail sent")

          case _ => Forbidden("Please verify your login details \n Try to login again" + "\n")
        })
      })
  }
}
