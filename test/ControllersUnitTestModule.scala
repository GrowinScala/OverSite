
import com.google.inject.AbstractModule
import database.repository._
import slick.jdbc.MySQLProfile.api._
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.ExecutionContext

class ControllersUnitTestModule extends AbstractModule {
  override def configure(): Unit = {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    implicit val db: Database = Database.forURL(
      "jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;",
      driver = "org.h2.Driver")

    bind(classOf[Database]).toInstance(db)
    bind(classOf[ChatRepository]).toInstance(new FakeChatRepositoryImpl())
    bind(classOf[UserRepository]).toInstance(new FakeUserRepositoryImpl())
    bind(classOf[EmailRepository]).toInstance(new FakeEmailRepositoryImpl())
  }
}