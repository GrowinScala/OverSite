import slick.jdbc.MySQLProfile.api._


val tables = new TablesMysql()
val db = Database.forConfig("mysql")

val emailTable = new tables.EmailTable(this: DBComponent =>

import slickDriver.api._

db.run()
)


trait BankInfoRepository extends BankInfoTable { this: DBComponent =>

  import driver.api._

  def create(bankInfo: BankInfo): Future[Int] = db.run { bankTableInfoAutoInc += bankInfo }