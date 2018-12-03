package api.controllers

import scala.concurrent.duration.FiniteDuration
import javax.inject._
import akka.actor.ActorSystem
import database.mappings.TablesMysql.Email
import play.api.libs.json.{JsResult, JsSuccess, JsValue}
import play.api.mvc._
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class EmailsController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem) (implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  def reads(json: JsValue): JsResult[Email] = {
    val emailID = (json \ "emailID").as[String]
    val chatID = (json \ "chatID").as[String]
    val fromAdress = (json \ "fromAdress").as[String]
    val dateOF = (json \ "dateOF").as[String]
    val header = (json \ "header").as[String]
    val body = (json \ "body").as[String]

    JsSuccess(Email(emailID,chatID,fromAdress,dateOF,header,body))
  }

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/users`.
    */

  def index =  Action{
    Ok("something")
  }


  def email(chatID : String) = Action { request =>
    val json = request.body.asJson.get
    //val email = json.as[Email]
    val email = reads(json)
    print(email)
    Ok
  }


}
