package repository

import api.dtos.CreateUserDTO
import database.mappings.ChatMappings.chatTable
import database.mappings.EmailMappings.{ bccTable, ccTable, emailTable, toAddressTable }
import database.mappings.UserMappings.{ loginTable, userTable }
import database.repository.UserRepositoryImpl
import definedStrings.AlgorithmStrings.MD5Algorithm
import definedStrings.testStrings.RepositoryStrings._
import encryption.EncryptString
import generators._
import org.scalatest._
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.H2Profile.api._
import org.scalatest.Matchers

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class UserRepositoryTest extends AsyncWordSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]

  val userActions = new UserRepositoryImpl()
  val userGenerator = new Generator()
  val userCreation = CreateUserDTO(userGenerator.username, userGenerator.password)
  val userCreationWrongPassword = new CreateUserDTO(userCreation.username, new Generator().password)
  val userCreationWrongUser = new CreateUserDTO(new Generator().username, userCreation.password)

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
  UserRepository + LoginTableFunction should {
    "check if the correct user is inserted in login table in database" in {
      userActions.insertUser(userCreation)
      val encrypt = new EncryptString(userCreation.password, MD5Algorithm)
      val resultUserTable = db.run(userTable.result)

      resultUserTable.map(seq => {
        //seq.forall(_.username === userCreation.username) shouldBe true
        seq.head.username shouldEqual userCreation.username
        seq.head.password shouldEqual encrypt.result.toString
      })
      //resultUserTable.map(_.map(user =>
      //assert((user.username, user.password) === (userCreation.username, encrypt.result.toString))))
    }
  }
  /*
  /** Test the insertion of an user into login database */
  UserRepository + InsertUserFunction should {
    "insert a correct user in database" in {
      userActions.insertUser(userCreation)
      val encrypt = new EncryptString(userCreation.password, MD5Algorithm)
      val resultLoginUser = userActions.loginUser(userCreation)

      /** Verify if user is inserted in login table correctly */
      resultLoginUser.map(_.map(user =>
        assert((user.username, user.password) === (userCreation.username, encrypt.result.toString))))
    }
  }

  /** Test the login of a available user */
  UserRepository + LoginUserFunction should {
    "login with a available user in database" in {
      userActions.insertUser(userCreation)
      userActions.insertLogin(userCreation)
      val resultLoginTable = db.run(loginTable.result)

      /** Verify if user is inserted in login table correctly */
      resultLoginTable.map(x => assert(x.head.username === userCreation.username))
    }
  }

  /** Test the login of an user with a wrong username*/
  UserRepository + LoginUserFunction should {
    "login with an unavailable username in database" in {
      userActions.insertUser(userCreation)
      userActions.insertLogin(userCreation)
      val resultLoginUser = userActions.loginUser(userCreationWrongUser)
      /** Verify if user is inserted in login table correctly */
      resultLoginUser.map(x => assert(x.isEmpty))
    }
  }

  /** Test the login of an user with a wrong password */
  UserRepository + LoginUserFunction should {
    "login with an unavailable password in database" in {
      userActions.insertUser(userCreation)
      userActions.insertLogin(userCreation)
      val resultLoginUser = userActions.loginUser(userCreationWrongPassword)

      /** Verify if user is inserted in login table correctly */
      resultLoginUser.map(x => assert(x.isEmpty))
    }
  }

  /** Test the logout of an user into database */
  UserRepository + LogoutUserFunction should {
    "logout with an available user in database" in {
      userActions.insertUser(userCreation)
      val token = userActions.insertLogin(userCreation)
      token.map(tkn => userActions.insertLogout(tkn))
      val resultLogOut = db.run(loginTable.result)

      /** Verify if the logout is processed correctly*/
      resultLogOut.map(_.map(x => assert(x.active === false)))
    }
  }

  /** Test the logout of an user into database with a wrong token*/
  UserRepository + LogoutUserFunction should {
    "logout with an available user in database with wrong token" in {
      userActions.insertUser(userCreation)
      userActions.insertLogout(new Generator().token)
      val resultLogOut = db.run(loginTable.result)

      /** Verify if the logout is processed correctly*/
      resultLogOut.map( seq =>
        _.map(x => assert(x.active === true))
      )
    }

  }
  */
}