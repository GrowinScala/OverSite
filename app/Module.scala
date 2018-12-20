import com.google.inject.AbstractModule
import slick.jdbc.MySQLProfile.api._

class  Module extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Database]).toInstance(Database.forConfig("mysql"))
  }
}