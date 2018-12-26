package api.controllers

import akka.actor.ActorSystem
import api.dtos.CreateEmailDTO
import api.validators.TokenValidator
import database.repository.{ ChatRepository, EmailRepository, UserRepository }
import javax.inject._
import play.api.libs.json
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._
import definedStrings.ApiStrings._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Class injected with end-points
 */

class EmailsController @Inject() (
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  implicit val db: Database,
  emailActions: EmailRepository,
  usersActions: UserRepository)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

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
          BadRequest(Json.obj(StatusJSONField -> ErrorString, MessageString -> JsError.toJson(errors)))
        }
      },
      email => {
        request.userName.map(
          emailActions.insertEmail(_, email))
        Future {
          Ok(MailSentStatus)
        }
      })
  }

  /**
   * Considers the case where the user wants to check some type of emails
   *
   * @param status End-point informations considering "draft", "received", "sent", "supervised" as allowed words
   * @return List of emails asked by the user
   */
  def getEmails(status: String): Action[AnyContent] = tokenValidator.async { request =>
    if (PossibleEndPointStatus.contains(status)) {
      request.userName.flatMap(
        emailActions.getEmails(_, status).map(
          emails => {
            val resultEmailID = JsArray(
              emails.map { x =>
                JsObject(Seq(
                  (EmailIDJSONField, JsString(x._1)),
                  (HeaderJSONField, JsString(x._2))))
              })
            Ok(resultEmailID)
          }))
    } else if (status == SatanString) {
      Future(BadRequest(SatanStatus))
    } else {
      Future(BadRequest(InvalidEndPointStatus))
    }
  }

  def getEmail(status: String, emailID: String): Action[AnyContent] = tokenValidator.async { request =>
    if (PossibleEndPointStatus.contains(status)) {
      request.userName.flatMap(
        emailActions.getEmail(_, status, emailID).map(
          email => {
            val resultEmailID = JsArray(
              email.map { x =>
                JsObject(Seq(
                  (EmailIDJSONField, JsString(emailID)),
                  (ChatIDJSONField, JsString(x._1)),
                  (FromAddressJSONField, JsString(x._2)),
                  (ToAddressJSONField, JsString(x._3)),
                  (HeaderJSONField, JsString(x._4)),
                  (BodyJSONField, JsString(x._5)),
                  (DateJSONField, JsString(x._6))))
              })
            Ok(resultEmailID)
          }))
    } else {
      Future { BadRequest(InvalidEndPointStatus) }
    }
  }

  def toSent(status: String, emailID: String): Action[AnyContent] = tokenValidator.async { request =>

    if (status.equals(EndPointDraft))
      request.userName.flatMap(
        emailActions.takeDraftMakeSent(_, emailID).map {
          case 0 => BadRequest
          case _ => Ok
        })
    else Future { BadRequest(InvalidEndPointStatus) }
  }
}
