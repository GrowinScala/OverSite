package api.validators
import akka.actor.ActorSystem
import akka.stream.{ ActorMaterializer, Materializer }
import database.mappings.UserMappings.loginTable
import javax.inject.{ Inject, Singleton }
import play.api.mvc
import play.api.mvc.Results._
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._
import definedStrings.ApiStrings._
import definedStrings.DatabaseStrings.OversiteDB

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

/** Case class created to replace the first parameter of ActionBuilder */
case class AuthRequest[A](
  userName: Future[String],
  request: Request[A]) extends WrappedRequest[A](request) {
  override def newWrapper[B](newRequest: Request[B]): AuthRequest[B] =
    AuthRequest(
      userName,
      super.newWrapper(newRequest))
}

/** Class responsible to validate the token */
class TokenValidator extends ActionBuilder[AuthRequest, AnyContent] {
  override protected def executionContext: ExecutionContext = global
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()
  override def parser: BodyParser[AnyContent] = new mvc.BodyParsers.Default()
  implicit val db: Database = Database.forConfig(OversiteDB)

  override def invokeBlock[A](request: Request[A], block: AuthRequest[A] => Future[Result]): Future[Result] = {

    val authToken = request.headers.get(TokenHeader).getOrElse(EmptyString)
    validateToken(authToken).flatMap {
      case true =>
        val userName = getUserByToken(authToken)
        block(AuthRequest(userName, request))

      case false =>
        Future { Forbidden(VerifyLoginStatus) }
    }
  }

  /**
   * Validates the userName and token inserted by the user
   * @param token token provided from the headers
   * @return boolean value considering of the token is valid or not
   */
  def validateToken(token: String): Future[Boolean] = {
    val validateTableToken = loginTable.filter(p => (p.token === token) && (p.active === true) && (p.validDate > System.currentTimeMillis())).result
    db.run(validateTableToken).map(_.length).map {
      case 1 => true
      case _ => false
    }
  }

  /**
   * Corresponds an token to an username
   * @param token token provided from the headers
   * @return Username associated to token
   */
  def getUserByToken(token: String): Future[String] = {
    val getUser = loginTable.filter(x => x.token === token).map(_.username).result
    db.run(getUser).map(_.head)
  }

}