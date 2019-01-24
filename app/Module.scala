import com.google.inject.AbstractModule
import database.repository._
import slick.jdbc.MySQLProfile.api._
import definedStrings.DatabaseStrings._

import scala.concurrent.ExecutionContext

/** Outline the database to be used implicitly */
class Module extends AbstractModule {
  override def configure(): Unit = {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    implicit val db: Database = Database.forConfig(OversiteDB)

    bind(classOf[Database]).toInstance(db)
    bind(classOf[ChatRepository]).toInstance(new ChatRepositoryImpl())
    bind(classOf[UserRepository]).toInstance(new UserRepositoryImpl())
    bind(classOf[EmailRepository]).toInstance(new EmailRepositoryImpl())
  }
}