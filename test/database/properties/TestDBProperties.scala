package database.properties

import javax.inject.Singleton
import slick.jdbc.MySQLProfile.api._

@Singleton
object TestDBProperties extends DBProperties {
  override val db: Database = Database.forURL(
    url = "jdbc:h2:mem:testdb;MODE=MYSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;",
    driver = "org.h2.Driver")
}