package database.repository

import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID.randomUUID

import api.dto.CreateUserDTO
import database.mappings.{ Login, User }
import database.mappings.UserMappings._
import encryption.EncryptString
import slick.jdbc.MySQLProfile.api._
import play.api.http.Status._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UserRepository(path: String) {
  val db = Database.forConfig(path)

  def insertUser(user: CreateUserDTO): Future[Int] = {
    val encrypt = new EncryptString(user.password)
    val insertTableEmail = UserTable += User(user.username, encrypt.result.toString)
    db.run(insertTableEmail)
  }

  def loginUser(user: CreateUserDTO) = {
    val encrypt = new EncryptString(user.password)
    val realUser = UserTable.filter(x => (x.username === user.username) && x.password === encrypt.result.toString).result
    db.run(realUser)
  }

  def insertLogin(user: CreateUserDTO) = {
    val token = randomUUID().toString
    val insertTableLogin = LoginTable += Login(user.username, token, validate1Hour)

    db.run(insertTableLogin)
    token
  }

  def validate1Hour = {
    val miliiSecs = System.currentTimeMillis()
    val sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm")
    //3600000 mili secs is 1 hour
    val resultDate = new Date(miliiSecs + 3600000)
    sdf.format(resultDate)
  }

}
