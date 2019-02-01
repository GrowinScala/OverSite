package api.controllers

import akka.actor.ActorSystem
import api.JsonObjects.jsonErrors
import api.dtos.AuxFunctions._
import api.dtos.{ CreateEmailDTO, MinimalInfoDTO, TrashInfoDTO }
import api.validators.TokenValidator
import database.repository.{ EmailRepository, UserRepository }
import definedStrings.ApiStrings._
import javax.inject._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/** Class injected with end-points */

@Singleton class EmailsController @Inject() (
  //dbclass: dbClass,
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  emailActions: EmailRepository,
  usersActions: UserRepository)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  //val tokenValidator: TokenValidator = new TokenValidator(dbclass.db)

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
            List(routes.EmailsController.getEmail(email.Id, status).absoluteURL())))
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
        emailActions.getEmail(_, status.getOrElse(""), emailID).map { email =>
          Ok(Json.toJson(convertEmailInfoToSender(email, emailID)))
        })
    } else {
      Future.successful { BadRequest(InvalidEndPointStatus) }
    }
  }

  /**
   * Change the email required to trash or take it from trash
   * @param emailID Identification of the email
   */
  def moveInOutTrash(emailID: String): Action[JsValue] = tokenValidator(parse.json).async { request =>
    val toTrashResult = request.body.validate[TrashInfoDTO]

    toTrashResult.fold(
      errors => {
        Future {
          BadRequest(jsonErrors(errors))
        }
      },
      move => {
        request.userName.flatMap(
          emailActions.moveInOutTrashEmail(_, emailID, move.toTrash))
        Future.successful {
          Ok
        }
      })
  }

  /**
   * Get the email that corresponds to the shareID and emailID inserted
   * @param shareID Identification of the share
   * @param emailID Identification of the email
   * @return All details of the email required
   */

  def getSharedEmail(shareID: String, emailID: String): Action[AnyContent] = tokenValidator.async { request =>

    request.userName.flatMap(
      emailActions.getSharedEmail(_, shareID, emailID).map { email =>
        Ok(Json.toJson(convertEmailInfoToShareSender(email, shareID, emailID)))
      })
  }
}
