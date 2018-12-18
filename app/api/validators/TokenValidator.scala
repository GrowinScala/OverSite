package api.validators
import akka.stream.Materializer
import database.mappings.UserMappings.LoginTable
import javax.inject.Inject
import play.api.mvc
import play.api.mvc.Results._
import play.api.mvc._
import slick.jdbc.MySQLProfile
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.MySQLProfile.backend.DatabaseDef

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ ExecutionContext, Future }

case class AuthRequest[A](
  userName: Future[String],
  request: Request[A]) extends WrappedRequest[A](request) {
  override def newWrapper[B](newRequest: Request[B]): AuthRequest[B] =
    AuthRequest(
      userName,
      super.newWrapper(newRequest))
}

class TokenValidator @Inject() (implicit mat: Materializer) extends ActionBuilder[AuthRequest, AnyContent] {

  override protected def executionContext: ExecutionContext = global
  override def parser: BodyParser[AnyContent] = new mvc.BodyParsers.Default()

  override def invokeBlock[A](request: Request[A], block: AuthRequest[A] => Future[Result]) = {

    val authToken = request.headers.get("Token").getOrElse("")

    validateToken(authToken).flatMap {
      case true =>
        val userName = getUserByToken(authToken)
        block(AuthRequest(userName, request))

      case false => Future { Forbidden("Please verify your login details \n Try to login again") }
    }
  }

  val db: DatabaseDef = Database.forConfig("mysql")

  /**
   * Validates the userName and token inserted by the user
   * @param token
   * @return
   */
  def validateToken(token: String) = {
    val validateTableToken = LoginTable.filter(x => (x.token === token) && x.validDate > System.currentTimeMillis()).result
    db.run(validateTableToken).map(_.length).map {
      case 1 => true
      case _ => false
    }
  }

  def getUserByToken(token: String) = {
    val getUser = LoginTable.filter(x => x.token === token).map(_.username).result
    db.run(getUser).map(_.head)
  }

}