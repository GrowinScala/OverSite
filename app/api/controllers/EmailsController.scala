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
 * @param cc
 * @param actorSystem
 * @param exec
 */
@Singleton
class EmailsController @Inject() (tokenValidator: TokenValidator, cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  val emailActions = new EmailRepository("mysql")
  val usersActions = new UserRepository("mysql")
  val chatActions = new ChatRepository("mysql")

  /**
   *
   * @param userName
   * @return
   */
  def email = tokenValidator(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateEmailDTO]

    emailResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
        }
      },
      email => {
        emailActions.insertEmail(email)
        Future { Ok("Mail sent") }
      })
  }

  def showEmails(status: String) = tokenValidator.async { request =>
    // TEMPORARILY VALUE
    val userName: String = "pppluis@cmail.com"
    emailActions.showEmails(userName, status).map(
      emails => {
        val resultEmailID = JsObject(emails.map(x => (x._1, JsString(x._2))))
        Ok(resultEmailID)
      })
  }
}
