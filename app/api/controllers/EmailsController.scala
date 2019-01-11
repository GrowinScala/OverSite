package api.controllers

import akka.actor.ActorSystem
import api.dtos.CreateEmailDTO
import api.validators.TokenValidator
import database.repository.{ EmailRepositoryImpl, UserRepositoryImpl }
import definedStrings.ApiStrings._
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._
import api.AuxFunctions._

import scala.concurrent.{ ExecutionContext, Future }

/** Class injected with end-points */

class EmailsController @Inject() (
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  implicit val db: Database,
  emailActions: EmailRepositoryImpl,
  usersActions: UserRepositoryImpl)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  /**
   * Aims to send an email from an user to an userID
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
        Future.successful {
          Ok(MailSentStatus)
        }
      })
  }

  /**
   * Considers the case where the user wants to check some type of emails
   * @param status End-point informations considering "draft", "received", "sent", "supervised" as allowed words
   * @return List of emails asked by the user
   */
  def getEmails(status: String): Action[AnyContent] = tokenValidator.async { request =>
    if (PossibleEndPointStatus.contains(status)) {
      request.userName.flatMap(emailActions.getEmails(_, status).map(emails => {
        Ok(Json.toJson(emails))
      }))
    } else if (status == SatanString) {
      Future.successful(BadRequest(SatanStatus))
    } else {
      Future.successful(BadRequest(InvalidEndPointStatus))
    }
  }

  /**
   * Selects an email after filtering through status and emailID
   * @param status Identification of the email status
   * @param emailID Identification of the email
   * @return Action that shows the emailID required
   */
  def getEmail(status: String, emailID: String): Action[AnyContent] = tokenValidator.async { request =>
    if (PossibleEndPointStatus.contains(status)) {
      request.userName.flatMap(
        emailActions.getEmail(_, status, emailID).map(
          emails => {
            val resultEmailID = JsArray(
              emails.map { email =>
                Json.toJson(convertEmailInfoToSender(email, emailID))
              })
            Ok(resultEmailID)
          }))
    } else {
      Future.successful { BadRequest(InvalidEndPointStatus) }
    }
  }

  /**
   * Receive a target draft email and sends it if that email has a to parameter
   * @param status Identification of the email status
   * @param emailID Identification of the email
   */
  def toSent(status: String, emailID: String): Action[AnyContent] = tokenValidator.async { request =>

    if (status.equals(EndPointDraft))
      request.userName.flatMap(
        emailActions.takeDraftMakeSent(_, emailID).map {
          case 0 => BadRequest
          case _ => Ok
        })
    else Future.successful { BadRequest(InvalidEndPointStatus) }
  }
}
