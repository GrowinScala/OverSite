package database.mappings
import slick.jdbc.MySQLProfile.api._

object User {
  case class User(
                   username: String,
                   password: String)
  class UserTable(tag:Tag) extends Table[User](tag, "users") {
    def username = column[String]("username", O.PrimaryKey)
    def password = column[String]("password")
    def * = (username, password) <> (User.tupled, User.unapply)
  }
  lazy val UserTable = TableQuery[UserTable]
}
