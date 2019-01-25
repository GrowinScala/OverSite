package api.controllers

import api.validators.TokenValidator
import database.repository.{ ChatRepository, _ }
import database.repository.fake.{ FakeChatRepositoryImpl, FakeUserRepositoryImpl }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{ Result, Results }
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.{ ExecutionContext, Future }

class ChatsControllerUnitTest extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterAll with BeforeAndAfterEach with Results {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val chatActions: ChatRepository = new FakeChatRepositoryImpl()
  val userActions: UserRepository = new FakeUserRepositoryImpl()

  "Example Page#index" should {
    "should be valid" in {
      val controller = new ChatController(
        TestAppBuilder.injector.instanceOf[TokenValidator],
        TestAppBuilder.cc,
        TestAppBuilder.actorSystem,
        chatActions,
        userActions)
      val result: Future[Result] = controller.getEmail("", "", Option(true)).apply(FakeRequest())
      status(result) mustBe OK
    }
  }

}