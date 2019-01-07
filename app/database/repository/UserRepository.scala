package database.repository

import api.dtos.CreateUserDTO

import scala.concurrent.Future

trait UserRepository {

  def insertUser(user: CreateUserDTO): Future[Int]
  def loginUser(user: CreateUserDTO): Future[Seq[CreateUserDTO]]
  def insertLogin(user: CreateUserDTO): Future[String]
  def insertLogout(token: String): Future[Int]
}
