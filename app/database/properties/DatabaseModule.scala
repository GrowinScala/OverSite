package database.properties

import slick.jdbc.JdbcProfile

trait DatabaseModule {

  val profile: JdbcProfile

  import profile.api._

}

object prodModule extends DatabaseModule {
  override val profile = slick.jdbc.MySQLProfile
}

object testModule extends DatabaseModule {
  override val profile = slick.jdbc.H2Profile
}
