package api.controllers

import akka.actor.ActorSystem
import api.dtos.CreateEmailDTO
import api.validators.TokenValidator
import database.repository.{ ChatRepository, EmailRepository, UserRepository }
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Class injected with end-points
 */

class EmailsController @Inject() (
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  db: Database
)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  //TODO: You should "rethink" using local instances and replace them by injections ;)
  val emailActions = new EmailRepository(db)
  val usersActions = new UserRepository(db)
  val chatActions = new ChatRepository(db)

  /**
   * Aims to send an email from an user to an userID
   *
   * @return inserts the email informations to the database
   */
  def email: Action[JsValue] = tokenValidator(parse.json).async { request =>
    val emailResult = request.body.validate[CreateEmailDTO]

    emailResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
        }
      },
      email => {
        request.userName.map(
          emailActions.insertEmail(_, email))
        Future {
          Ok("Mail sent")
        }
      })
  }

  /**
   * Considers the case where the user wants to check some type of emails
   *
   * @param status End-point informations considering "draft", "received", "sent", "supervised" as allowed words
   * @return List of emails asked by the user
   */
  def showEmails(status: String): Action[AnyContent] = tokenValidator.async { request =>
    val possibleStatus = List("draft", "received", "sent")
    if (possibleStatus.contains(status)) {
      request.userName.flatMap(
        emailActions.showEmails(_, status).map(
          emails => {
            val resultEmailID = JsObject(emails.map(x => (x._1, JsString(x._2))))
            Ok(resultEmailID)
          }))
    } else if (status == "satan") {
      Future(BadRequest("BURN YOUR LOCAL CHURCH"))
    } else {
      Future(BadRequest("Invalid status"))
    }
  }

}
