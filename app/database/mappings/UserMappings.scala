package database.mappings
import database.mappings.UserMappings.UserTable
import slick.jdbc.MySQLProfile.api._

//TODO: Consider renaming "validDate" both in the Row object and the DB to "expiryDate" instead.
/**
  * Case class of Login Table Row
  */
case class LoginRow(
  username: String,
  token: String,
  validDate: Long
)

/**
  * Case class of User Table Row
  */
case class UserRow(
  username: String,
  password: String
)

/**
 * Class that defines the login table, making username a foreign key in the database
 * @param tag slick tag
 */
class LoginTable(tag: Tag) extends Table[LoginRow](tag, "logins") {
  //TODO Insert userID to improve the search
  def username = column[String]("username")
  def token = column[String]("token")
  def validDate = column[Long]("validDate")
  def fileIdFK = foreignKey("username", username, UserTable)(_.username, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (username, token, validDate) <> (LoginRow.tupled, LoginRow.unapply)
}

/**
 * Class that defines the user table, making username a primary key in the database
 */
class UserTable(tag: Tag) extends Table[UserRow](tag, "users") {
  def username = column[String]("username", O.PrimaryKey)
  def password = column[String]("password")

  def * = (username, password) <> (UserRow.tupled, UserRow.unapply)
}

/**
 * Queries of user table and login table
 */
object UserMappings {
  lazy val UserTable = TableQuery[UserTable]
  lazy val LoginTable = TableQuery[LoginTable]
}
