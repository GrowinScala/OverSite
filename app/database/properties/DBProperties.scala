package database.properties

import definedStrings.DatabaseStrings._
import javax.inject.Singleton
import slick.jdbc.MySQLProfile.api._

trait DBProperties {
  val db: Database
}

@Singleton
object ProdDBProperties extends DBProperties {
  override val db: Database = Database.forConfig(OversiteDB)
}