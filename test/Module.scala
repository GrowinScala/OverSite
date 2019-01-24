import com.google.inject.AbstractModule
import database.repository._
import definedStrings.DatabaseStrings.OversiteDB
import play.api.Mode
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext

/** Outline the database to be used implicitly */
class Module extends AbstractModule {
  override def configure(): Unit = {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    implicit val db: Database = Database.forURL(
      "jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;",
      driver = "org.h2.Driver")

    bind(classOf[Database]).toInstance(db)
    bind(classOf[ChatRepository]).toInstance(new ChatRepositoryImpl())
    bind(classOf[UserRepository]).toInstance(new UserRepositoryImpl())
    bind(classOf[EmailRepository]).toInstance(new EmailRepositoryImpl())
  }
}
