package api.controllers

import akka.actor.ActorSystem
import api.dto.{ CreateEmailDTO, CreateEmailProfileDTO }
import api.validators.TokenValidator
import database.repository.{ ChatRepository, EmailRepository, UserRepository }
import javax.inject._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Class that is injected with end-points
 */
@Singleton
class EmailsController @Inject() (tokenValidator: TokenValidator, cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  val emailActions = new EmailRepository("mysql")
  val usersActions = new UserRepository("mysql")
  val chatActions = new ChatRepository("mysql")

  def index: Action[AnyContent] = tokenValidator.async {
    Future { Ok("") }
  }

  /**
   *
   * @param userName username from json body
   * @return
   */
  def email: Action[JsValue] = tokenValidator(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateEmailDTO]
    val authToken = request.headers.get("Token").getOrElse("")
    println(authToken)
    usersActions.getUserByToken(authToken).map(println(_))
    emailResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
        }
      },
      email => {
        usersActions.getUserByToken(authToken).map(emailActions.insertEmail(email, _))
        Future { Ok("Mail sent") }
      })
  }

  def showEmails(userName: String, status: String): Action[AnyContent] = tokenValidator.async { request =>
    println(request.path)
    emailActions.showEmails(userName, status).map(
      emails => {
        val resultEmailID = JsObject(emails.map(x => (x._1, JsString(x._2))))
        Ok(resultEmailID)
      })
  }
}
