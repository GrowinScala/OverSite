package api.controllers

import akka.actor.ActorSystem
import api.JsonObjects.jsonErrors
import api.dtos.AuxFunctions._
import api.dtos.{ CreateEmailDTO, MinimalInfoDTO }
import api.validators.TokenValidator
import database.repository.{ EmailRepositoryImpl, UserRepositoryImpl }
import definedStrings.ApiStrings._
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._

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
   * @return inserts the email information to the database
   */
  def email: Action[JsValue] = tokenValidator(parse.json).async { request =>
    val emailResult = request.body.validate[CreateEmailDTO]

    emailResult.fold(
      errors => {
        Future {
          BadRequest(jsonErrors(errors))
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
   * @param status End-point information considering "received", "sent", "trashed" as allowed words
   * @return List of emails asked by the user
   */
  def getEmails(status: Option[String]): Action[AnyContent] = tokenValidator.async { request =>

    implicit val req: RequestHeader = request

    if (PossibleEndPointStatus.contains(status.getOrElse(""))) {
      request.userName.flatMap(emailActions.getEmails(_, status.getOrElse("")).map(emails => {
        val result = emails.map(email =>
          MinimalInfoDTO.addLink(
            email,
            //List(routes.EmailsController.getEmail(email.Id, status).absoluteURL())))
            List("")))
        Ok(Json.toJson(result))
      }))
    } else if (status.getOrElse("") == SatanString) {
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
  def getEmail(emailID: String, status: Option[String]): Action[AnyContent] = tokenValidator.async { request =>

    implicit val req: RequestHeader = request

    if (PossibleEndPointStatus.contains(status.getOrElse(""))) {
      request.userName.flatMap(
        emailActions.getEmail(_, status.getOrElse(""), emailID).map(
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
   * Change the email required to trash or take it from trash
   * @param status Identification of the email status
   * @param emailID Identification of the email
   */
  def moveInOutTrash(emailID: String): Action[AnyContent] = tokenValidator.async { request =>

    request.userName.flatMap(
      emailActions.changeTrash(_, emailID).map {
        case 0 => BadRequest
        case _ => Ok
      })
  }
}
