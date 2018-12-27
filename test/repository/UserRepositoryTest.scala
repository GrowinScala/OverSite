package repository

import api.dtos.CreateUserDTO
import org.scalatest._
import org.scalatestplus.play.PlaySpec
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import repository.actions.UserActions
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class UserRepositoryTest extends PlaySpec with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]

  val userActionsTest = new UserActions()

  override def beforeAll() = {
    userActionsTest.createFilesTable
  }

  override def afterAll() = {
    userActionsTest.dropFilesTable
  }

  //insertUserTest(user: CreateUserDTO)
  "UsersRepository #insertUser" should {
    "insert a correct user in database" in {
      val userCreation = new CreateUserDTO("pedro@hotmail.com", "12345")
      userActionsTest.insertUserTest(userCreation).map(_ mustBe true)
    }
  }
}