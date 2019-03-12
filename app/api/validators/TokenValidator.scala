package api.validators
import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, Materializer }
import definedStrings.ApiStrings._
import play.api.mvc
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

trait TokenValidator extends ActionBuilder[AuthRequest, AnyContent] {

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()

  def validateToken(token: String): Future[Boolean]
  def getUserByToken(token: String): Future[String]

  override protected def executionContext: ExecutionContext = global

  override def parser: BodyParser[AnyContent] = new mvc.BodyParsers.Default()

  override def invokeBlock[A](request: Request[A], block: AuthRequest[A] => Future[Result]): Future[Result] = {

    val authToken = request.headers.get(TokenHeader).getOrElse(EmptyString)

    validateToken(authToken).flatMap {
      case true =>
        val userName = getUserByToken(authToken)
        block(AuthRequest(userName, request))

      case false => Future.successful { Forbidden(VerifyLoginStatus) }
    }
  }

}

