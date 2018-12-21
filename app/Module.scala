import com.google.inject.AbstractModule
import slick.jdbc.MySQLProfile.api._
import definedStrings.DatabaseStrings._

class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Database]).toInstance(Database.forConfig(OversiteDB))
  }
}