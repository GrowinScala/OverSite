package database.mappings

import slick.jdbc.MySQLProfile.api._
import database.mappings.UserMappings._

case class User(
  username: String,
  password: String)

case class Login(
  username: String,
  token: String,
  validDate: String)

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def username = column[String]("username", O.PrimaryKey)

  def password = column[String]("password")

  def * = (username, password) <> (User.tupled, User.unapply)
}

class LoginTable(tag: Tag) extends Table[Login](tag, "logins") {
  def username = column[String]("username")

  def token = column[String]("token")

  def validDate = column[String]("validDate")

  def fileIdFK = foreignKey("username", username, UserTable)(_.username, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  def * = (username, token, validDate) <> (Login.tupled, Login.unapply)
}

object UserMappings {
  lazy val UserTable = TableQuery[UserTable]
  lazy val LoginTable = TableQuery[LoginTable]
}
