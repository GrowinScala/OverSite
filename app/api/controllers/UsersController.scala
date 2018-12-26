package api.controllers

import akka.actor.ActorSystem
import api.dtos.CreateUserDTO
import api.validators.EmailAddressValidator._
import api.validators.TokenValidator
import database.repository.UserRepository
import definedStrings.ApiStrings._
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import regex.RegexPatterns.emailAddressPattern
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

/** Class that is injected with end-points */

class UsersController @Inject() (
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  tokenValidator: TokenValidator,
  implicit val db: Database,
  userActions: UserRepository)(implicit exec: ExecutionContext)

  extends AbstractController(cc) {

  /**
   * Sign in action
   * @return When a valid user is inserted, it is added in the database, otherwise an error message is sent
   */
  def signIn: Action[JsValue] = Action(parse.json).async { request: Request[JsValue] =>
    val userResult = request.body.validate[CreateUserDTO]

    userResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj(StatusJSONField -> ErrorString, MessageString -> JsError.toJson(errors)))
        }
      },
      user => {
        if (validateEmailAddress(emailAddressPattern, Left(user.username))) {
          userActions.insertUser(user)
          Future {
            Created
          }
        } else Future { BadRequest(InvalidEmailAddressStatus) }
      })
  }

  /**
   * Login action
   * @return When a valid login is inserted, it is added in the database
   *         and the generated token is sent to user, otherwise an error message is sent
   */
  def logIn: Action[JsValue] = Action(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateUserDTO]
    // Getting the token from the request API call

    emailResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj(StatusJSONField -> ErrorString, MessageString -> JsError.toJson(errors)))
        }
      },
      user => {
        val loggedUser = userActions.loginUser(user)
        loggedUser.map(_.length).map {
          case 1 => Ok(JsObject(Seq(
            (TokenJSONField, JsString(userActions.insertLogin(user))),
            (TokenValidTimeJsonField, JsString(Token1HourValid)))))
          case _ => Forbidden(PasswordMissMatchStatus)
        }
      })
  }

  /**
   * Logout action
   * @return When a logout is called, the "active" parameter is turned down
   */
  def logOut: Action[AnyContent] = tokenValidator.async { request =>
    val authToken = request.headers.get(TokenHeader).getOrElse("")
    userActions.insertLogout(authToken).map {
      case 1 => Ok
      case _ => NotModified
    }
  }
}
