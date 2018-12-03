import TablesMysql._
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration._


class Actions {

  val db = Database.forConfig("mysql")

  //--------------------------------------Insert into db--------------------------------------------------------------------------

  val insertChat = ChatTable += Chat( "3"  , "Hello World!" )
  val insertEmail = EmailTable += Email( "1"  , "3" , "rvalente@growin.pt","2018-12-02","Hello World!", "How u doin?")

  /**
    * Execute an action to configured db
    * @param action
    * @tparam T
    * @return
    */
  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 50 seconds)
}
