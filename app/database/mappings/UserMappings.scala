package database.mappings
import database.mappings.UserMappings.UserTable
import slick.jdbc.MySQLProfile.api._

case class Login(
  username: String,
  token: String,
  validDate: Long)

/**
 * Class that defines the login table, making username a foreign key in the database
 * @param tag slick tag
 */
class LoginTable(tag: Tag) extends Table[Login](tag, "logins") {
  //TODO Insert userID to improve the search
  def username = column[String]("username")
  def token = column[String]("token")
  def validDate = column[Long]("validDate")
  def fileIdFK = foreignKey("username", username, UserTable)(_.username, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  def * = (username, token, validDate) <> (Login.tupled, Login.unapply)
}

case class User(
  username: String,
  password: String)

/**
 * Class that defines the user table, making username a primary key in the database
 */
class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def username = column[String]("username", O.PrimaryKey)
  def password = column[String]("password")
  def * = (username, password) <> (User.tupled, User.unapply)
}

/**
 * Queries of user table and login table
 */
object UserMappings {
  lazy val UserTable = TableQuery[UserTable]
  lazy val LoginTable = TableQuery[LoginTable]
}
