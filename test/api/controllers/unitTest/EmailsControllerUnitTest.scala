package api.controllers.unitTest

import database.repository.fake.{ FakeChatRepositoryImpl, FakeEmailRepositoryImpl, FakeUserRepositoryImpl }
import org.scalatest.{ Matchers, _ }

class EmailsControllerUnitTest extends AsyncWordSpec with BeforeAndAfterAll with BeforeAndAfterEach with Matchers {

  val fakeChatActions = new FakeChatRepositoryImpl
  val fakeEmailActions = new FakeEmailRepositoryImpl
  val fakeUserActions = new FakeUserRepositoryImpl

}
