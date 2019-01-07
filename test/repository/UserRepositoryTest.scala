package repository

import actions.UserActions
import api.dtos.CreateUserDTO
import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.UserMappings.{ loginTable, userTable }
import database.repository.UserRepository
import encryption.EncryptString
import org.scalatest._
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._
import definedStrings.AlgorithmStrings.MD5Algorithm

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class UserRepositoryTest extends WordSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]

  val userActions = new UserRepository()

  //val userActionsTest = new UserActions()
  val userCreation = new CreateUserDTO("rvalente@growin.com", "12345")
  val userCreationWrongPassword = new CreateUserDTO("rvalente@growin.com", "00000")
  val userCreationWrongUser = new CreateUserDTO("pluis@growin.com", "12345")

  val tables = Seq(chatTable, userTable, emailTable, toAddressTable, ccTable, bccTable, loginTable)

  override def beforeAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.create): _*)), Duration.Inf)
  }

  override def afterAll(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.schema.drop): _*)), Duration.Inf)
  }

  override def afterEach(): Unit = {
    Await.result(db.run(DBIO.seq(tables.map(_.delete): _*)), Duration.Inf)
  }

  /** Verify if an user has signed in into database */
  "UsersRepository #loginTable" should {
    "check if the correct user is inserted in login table in database" in {
      Await.result(userActions.insertUser(userCreation), Duration.Inf)
      val encrypt = new EncryptString(userCreation.password, MD5Algorithm)
      val resultUserTable = Await.result(db.run(userTable.result), Duration.Inf)
      resultUserTable.map(user =>
        assert((user.username, user.password) === (userCreation.username, encrypt.result.toString)))
    }
  }

  /** Test the insertion of an user into login database */
  "UsersRepository #insertUser" should {
    "insert a correct user in database" in {
      Await.result(userActions.insertUser(userCreation), Duration.Inf)
      val encrypt = new EncryptString(userCreation.password, MD5Algorithm)
      val resultLoginUser = Await.result(userActions.loginUser(userCreation), Duration.Inf)

      /** Verify if user is inserted in login table correctly */
      resultLoginUser.map(user =>
        assert((user.username, user.password) === (userCreation.username, encrypt.result.toString)))
    }
  }

  /** Test the login of a available user */
  "UsersRepository #loginUser" should {
    "login with a available user in database" in {
      Await.result(userActions.insertUser(userCreation), Duration.Inf)
      Await.result(userActions.insertLogin(userCreation), Duration.Inf)
      val resultLoginTable = Await.result(db.run(loginTable.result), Duration.Inf)

      /** Verify if user is inserted in login table correctly */
      assert(resultLoginTable.head.username === userCreation.username)
    }
  }

  /*
  /** Test the login of an user with a wrong username*/
  "UsersRepository #loginUser" should {
    "login with an unavailable username in database" in {
      val result = userActionsTest.insertLoginTest(userCreation, userCreationWrongUser)
      assert(result === false)
    }
  }

  /** Test the login of an user with a wrong password */
  "UsersRepository #loginUser" should {
    "login with an unavailable password in database" in {
      val result = userActionsTest.insertLoginTest(userCreation, userCreationWrongPassword)
      assert(result === false)
    }
  }

  /** Test the logout of an user into database */
  "UsersRepository #logoutUser" should {
    "logout with an available user in database" in {
      val result = userActionsTest.insertLogoutTest(userCreation, None, None)
      assert(result === true)
    }
  }

  /** Test the logout of an user into database with a wrong token*/
  "UsersRepository #logoutUser" should {
    "logout with an available user in database with wrong token" in {
      val result = userActionsTest.insertLogoutTest(userCreation, Option("00000"), None)
      assert(result === false)
    }
  }

  /** Test the logout of an user into database with wrong boolean for active*/
  "UsersRepository #logoutUser" should {
    "logout with an available user in database with wrong boolean for active" in {
      val result = userActionsTest.insertLogoutTest(userCreation, None, Option(true))
      assert(result === false)
    }
  }
*/
}

