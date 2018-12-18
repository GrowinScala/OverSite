package api.validators

import database.mappings.UserMappings.LoginTable
import play.api.libs.json.JsValue
import play.api.mvc.Results.Forbidden
import play.api.mvc._
import play.mvc.Http.RequestHeader
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ ExecutionContext, Future }

object TokenValidator2 {

  val db = Database.forConfig("mysql")

  //def AuthenticatedAction[A]: ActionBuilder[Request[JsValue], AnyContent] = ActionBuilder[Request[JsValue], AnyContent]
  /* extends ActionBuilder[Request,AnyContent]
 /efaultControllerComponents
 DefaultActionBuilder
 ControllerComponents
 def Action: ActionBuilder[Request, AnyContent] = controllerComponents.actionBuilder
*/
  /* = {

   val authToken = request.headers.get("Token").getOrElse("")

   validateToken(authToken).flatMap {
     case true => ActionBuilder[RequestHeader[String], AnyContent]
     case false => Future { Forbidden("Please verify your login details \n Try to login again") }
   }*/

  def validateToken(token: String)(implicit ec: ExecutionContext) = {
    val validateTableToken = LoginTable.filter(x => (x.token === token) && x.validDate > System.currentTimeMillis()).result
    db.run(validateTableToken).map(_.length).map {
      case 1 => true
      case _ => false
    }
  }
}
