package api.validators
import database.mappings.UserMappings.loginTable
import database.properties.DBProperties
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/** Class responsible to validate the token */
class ProdTokenValidator(dbClass: DBProperties) extends TokenValidator {

  val db = dbClass.db
  /**
   * Validates the userName and token inserted by the user
   * @param token token provided from the headers
   * @return boolean value considering of the token is valid or not
   */
  def validateToken(token: String): Future[Boolean] = {
    val validateTableToken = loginTable.filter(entry => (entry.token === token) && (entry.active === true) && (entry.validDate > System.currentTimeMillis())).result
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
    db.run(getUser).map(_.headOption.getOrElse(""))
  }
}