package database.repository

import api.dtos.CreateUserDTO
import definedStrings.testStrings.RepositoryStrings.EmptyString

import scala.concurrent.Future

class FakeUserRepositoryImpl extends UserRepository {

  def insertUser(user: CreateUserDTO): Future[Int] = {
    Future.successful(1)
  }
  def loginUser(user: CreateUserDTO): Future[Seq[CreateUserDTO]] = {
    Future.successful(Seq(CreateUserDTO(user.username, user.password)))
  }
  def insertLogin(user: CreateUserDTO): Future[String] = {
    Future.successful(EmptyString)
  }
  def insertLogout(token: String): Future[Int] = {
    Future.successful(1)
  }

}
