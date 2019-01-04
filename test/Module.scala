import com.google.inject.AbstractModule
import slick.jdbc.MySQLProfile.api._

/** Outline the database to be used implicitly */
class Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Database]).toInstance(Database.forURL(
      "jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;",
      driver = "org.h2.Driver"))
  }
}
