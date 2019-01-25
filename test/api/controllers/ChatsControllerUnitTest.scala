package api.controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import api.validators.{ MockedTokenValidator, TokenValidator }
import database.repository.{ ChatRepository, _ }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.Injector
import play.api.mvc.{ ControllerComponents, Result, Results }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ ExecutionContext, Future }

class ChatsControllerUnitTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach with Results {

  val chatActions: ChatRepository = new FakeChatRepositoryImpl()
  val userActions: UserRepository = new FakeUserRepositoryImpl()

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  //lazy val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)

  import play.api.inject.bind
  import play.api.inject.guice.GuiceApplicationBuilder

  val appBuilder = new GuiceApplicationBuilder()
    .load(
      new play.api.inject.BuiltinModule,
      new play.api.i18n.I18nModule,
      new play.api.mvc.CookiesModule,
      bind(classOf[TokenValidator]).toInstance(new MockedTokenValidator),
      bind(classOf[ChatRepository]).toInstance(new FakeChatRepositoryImpl()),
      bind(classOf[UserRepository]).toInstance(new FakeUserRepositoryImpl()),
      bind(classOf[EmailRepository]).toInstance(new FakeEmailRepositoryImpl()))

  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val db: Database = injector.instanceOf[Database]
  lazy implicit val mat = injector.instanceOf[Materializer]
  lazy val cc = injector.instanceOf[ControllerComponents]
  val actorSystem = injector.instanceOf[ActorSystem]

  //lazy implicit val user = injector.instanceOf[UserRepository]
  //lazy implicit val chat = injector.instanceOf[ChatRepository]

  "Example Page#index" should {
    "should be valid" in {
      val controller = new ChatController(
        injector.instanceOf[TokenValidator],
        cc,
        actorSystem, chatActions, userActions)
      val result: Future[Result] = controller.getEmail("", "", Option(true)).apply(FakeRequest())
      val bodyText = status(result)
      bodyText mustBe OK
    }
  }

}