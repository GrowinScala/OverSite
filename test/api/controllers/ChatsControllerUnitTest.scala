package api.controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import api.dtos.{ CreateEmailDTO, CreateShareDTO, CreateUserDTO }
import api.validators.TokenValidator
import database.mappings.ChatMappings._
import database.mappings.DraftMappings.destinationDraftTable
import database.mappings.EmailMappings._
import database.mappings.UserMappings._
import database.repository._
import definedStrings.testStrings.RepositoryStrings._
import generators.Generator
import javax.inject.Inject
import org.scalatest.{ Matchers, _ }
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.ControllerComponents
import slick.jdbc.H2Profile.api._
import ActorSystem._
import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import com.google.inject.AbstractModule

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

class ChatsControllerUnitTest extends AsyncWordSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {

  val fakeChatActions = new FakeChatRepositoryImpl
  val fakeEmailActions = new FakeEmailRepositoryImpl
  val fakeDraftActions = new FakeDraftRepositoryImpl
  val fakeUserActions = new FakeUserRepositoryImpl

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]
  lazy implicit val mat = injector.instanceOf[Materializer]
  lazy val cc = injector.instanceOf[ControllerComponents]
  val actorSystem = injector.instanceOf[ActorSystem]

  val chatActions: ChatRepository = new ChatRepositoryImpl()
  val userActions: UserRepository = new UserRepositoryImpl()
  val tokenValidator = new TokenValidator()

  import play.api.inject.bind
  appBuilder.overrides(bind[UserRepository].toInstance(userActions))
    .overrides(bind[ChatRepository].toInstance(chatActions))
    .build()

  val chatController = new ChatController(tokenValidator, cc,
    actorSystem, db, chatActions, userActions)

  chatController.getEmail("", "", Option(true))

}