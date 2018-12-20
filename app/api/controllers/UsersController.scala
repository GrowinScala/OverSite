package api.controllers

import akka.actor.ActorSystem
import api.dto.CreateUserDTO
import database.repository.UserRepository
import javax.inject._
import play.api.libs.json.{ JsError, JsValue, Json }
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Class that is injected with end-points
 */

class UsersController @Inject() (
  cc: ControllerComponents,
  actorSystem: ActorSystem,
  db: Database)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  val userActions = new UserRepository(db)

  /**
   * Sign in action
   *
   * @return When a valid user is inserted, it is added in the database, otherwise an error message is sent
   */
  def signin: Action[JsValue] = Action(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateUserDTO]
    emailResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
        }
      },
      user => {
        userActions.insertUser(user)
        Future {
          Created
        }
      })
  }

  /**
   * Login action
   *
   * @return When a valid login is inserted, it is added in the database
   *         and the generated token is sent to user, otherwise an error message is sent
   */
  def login: Action[JsValue] = Action(parse.json).async { request: Request[JsValue] =>
    val emailResult = request.body.validate[CreateUserDTO]
    // Getting the token from the request API call
    emailResult.fold(
      errors => {
        Future {
          BadRequest(Json.obj("status" -> "Error:", "message" -> JsError.toJson(errors)))
        }
      },
      user => {
        val loggedUser = userActions.loginUser(user)
        loggedUser.map(_.length).map {
          case 1 => Ok("Your token is: " + userActions.insertLogin(user) + "\n The token is valid for 1 hour")
          case x => Forbidden("Username and password doesn´t match" + x)
        }
      })
  }
}
