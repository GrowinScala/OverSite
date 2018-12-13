package api.validators
import database.mappings.UserMappings.LoginTable
import javax.inject.Inject
import play.api.mvc.Results._
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

class TokenValidator @Inject() (parser: BodyParsers.Default)(implicit ec: ExecutionContext) extends ActionBuilderImpl(parser) {
  override def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {

    val authToken = request.headers.get("Token").getOrElse("")
    validateToken(authToken).flatMap {
      case true => block(request)
      case false => Future { Forbidden("Please verify your login details \n Try to login again") }
    }
  }

  val db = Database.forConfig("mysql")

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
}