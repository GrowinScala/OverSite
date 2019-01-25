package api.controllers
import database.repository.fake.{ FakeChatRepositoryImpl, FakeDraftRepositoryImpl, FakeEmailRepositoryImpl, FakeUserRepositoryImpl }
import org.scalatest.{ Matchers, _ }

class UsersControllerUnitTest extends AsyncWordSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {

  val fakeChatActions = new FakeChatRepositoryImpl
  val fakeEmailActions = new FakeEmailRepositoryImpl
  val fakeDraftActions = new FakeDraftRepositoryImpl
  val fakeUserActions = new FakeUserRepositoryImpl

}
