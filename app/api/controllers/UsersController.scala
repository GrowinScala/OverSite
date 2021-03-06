package api.controllers

import akka.actor.ActorSystem
import api.JsonObjects._
import api.dtos.CreateUserDTO
import api.validators.EmailAddressValidator._
import api.validators.TokenValidator
import database.repository.UserRepository
import definedStrings.ApiStrings._
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import regex.RegexPatterns.emailAddressPattern

import scala.concurrent.{ ExecutionContext, Future }

/** Class that is injected with end-points */

@Singleton class UsersController @Inject() (
  tokenValidator: TokenValidator,
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  userActions: UserRepository)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  /**
   * Sign in action
   * @return When a valid user is inserted, it is added in the database, otherwise an error message is sent
   */
  def signIn: Action[JsValue] = Action(parse.json).async { request: Request[JsValue] =>

    val userResult = request.body.validate[CreateUserDTO]
    implicit val req: RequestHeader = request

    userResult.fold(

      errors => Future {
        BadRequest(jsonErrors(errors))
      },
      user => {
        validateEmailAddress(emailAddressPattern, Left(user.username)).map {

          case true =>
            userActions.insertUser(user)
            Created

          case false => BadRequest(InvalidEmailAddressStatus)
        }
      })
  }

  /**
   * Login action
   * @return When a valid login is inserted, it is added in the database
   *         and the generated token is sent to user, otherwise an error message is sent
   */
  def logIn: Action[JsValue] = Action(parse.json).async { request: Request[JsValue] =>

    implicit val req: RequestHeader = request
    val emailResult = request.body.validate[CreateUserDTO]

    emailResult.fold(

      errors => Future.successful { BadRequest(jsonErrors(errors)) },
      user => {
        val loggedUser = userActions.loginUser(user)

        loggedUser.map(_.length).flatMap {

          case 1 =>
            userActions.insertLogin(user).map(token =>
              Ok(JsObject(Seq((TokenJSONField, JsString(token)), (TokenValidTimeJsonField, JsString(Token2HourValid))))))

          case _ => Future.successful(Forbidden(PasswordMissMatchStatus))
        }
      })
  }

  /**
   * Logout action
   * @return When a logout is called, the "active" parameter is turned down
   */
  def logOut: Action[AnyContent] = tokenValidator.async { request =>

    val authToken = request.headers.get(TokenHeader).getOrElse(EmptyString)

    userActions.insertLogout(authToken).map {
      case 1 => Ok
      case _ => NotModified
    }
  }
}
