package repository

import actions.UserActions
import api.dtos.CreateUserDTO
import org.scalatest._
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext

class UserRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]

  val userActionsTest = new UserActions()
  val userCreation = new CreateUserDTO("rvalente@growin.com", "12345")
  val userCreationWrongPassword = new CreateUserDTO("rvalente@growin.com", "00000")
  val userCreationWrongUser = new CreateUserDTO("pluis@growin.com", "12345")

  override def beforeAll() = {
    userActionsTest.createFilesTable
  }

  override def afterAll() = {
    userActionsTest.dropFilesTable
  }

  override def afterEach(): Unit = {
    userActionsTest.deleteRowsTable
  }

  /** Verify if an user has signed in into database */
  "UsersRepository #loginTable" should {
    "check if the correct user is inserted in login table in database" in {
      userActionsTest.insertUserTest(userCreation).map(x => assert(x === true))
    }
  }
  /** Test the insertion of an user into database */
  "UsersRepository #insertUser" should {
    "insert a correct user in database" in {
      userActionsTest.loginUserTest(userCreation).map(x => assert(x === true))
    }
  }

  /** Test the login of a available user */
  "UsersRepository #loginUser" should {
    "login with a available user in database" in {
      userActionsTest.insertLoginTest(userCreation, userCreation).map(x => assert(x === true))
    }
  }

  /** Test the login of an user with a wrong username*/
  "UsersRepository #loginUser" should {
    "login with an unavailable username in database" in {
      userActionsTest.insertLoginTest(userCreation, userCreationWrongUser).map(x => assert(x === false))
    }
  }

  /** Test the login of an user with a wrong password */
  "UsersRepository #loginUser" should {
    "login with an unavailable password in database" in {
      userActionsTest.insertLoginTest(userCreation, userCreationWrongPassword).map(x => assert(x === false))
    }
  }

  /** Test the logout of an user into database */
  "UsersRepository #logoutUser" should {
    "logout with an available user in database" in {
      userActionsTest.insertLogoutTest(userCreation, None, None).map(x => assert(x === true))
    }
  }

  /** Test the logout of an user into database with a wrong token*/
  "UsersRepository #logoutUser" should {
    "logout with an available user in database with wrong token" in {
      userActionsTest.insertLogoutTest(userCreation, Option("00000"), None).map(x => assert(x === false))
    }
  }

  /** Test the logout of an user into database with wrong boolean for active*/
  "UsersRepository #logoutUser" should {
    "logout with an available user in database with wrong boolean for active" in {
      userActionsTest.insertLogoutTest(userCreation, None, Option(true)).map(x => assert(x === false))
    }
  }

}

