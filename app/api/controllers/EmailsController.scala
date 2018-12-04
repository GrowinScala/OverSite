package api.controllers

import scala.concurrent.duration.FiniteDuration
import javax.inject._
import akka.actor.ActorSystem
import akka.stream.Client
import database.mappings.EmailObject._
import play.api.libs.json.{JsResult, JsSuccess, JsValue}
import play.api.mvc._
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

case class CreateEmailDTO (
      emailID: String,
      chatID: Option[String],
      fromAdress: String,
      dateOf: String,
      header: Option[String],
      body : Option[String],
      sendNow : String
      )


//implicit reads(){}

@Singleton
class EmailsController @Inject()(cc: ControllerComponents, actorSystem: ActorSystem) (implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  def reads(request: Request[JsValue])  = {

    val emailID = (request.body \ "emailID").as[String]
    val chatID = (request.body \ "chatID").as[String]
    val fromAdress = (request.body \ "fromAdress").as[String]
    val dateOf = (request.body \ "dateOf").as[String]
    val header = (request.body \ "header").as[String]
    val body = (request.body \ "body").as[String]

    Email(emailID,chatID,fromAdress,dateOf,header,body)
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


  def email(userName: String) = Action(parse.json) { request: Request[JsValue]  =>
    request.body.validate[CreateEmailDTO]
    execDB( insertEmail(reads(request)) )
    Ok
  }

}
