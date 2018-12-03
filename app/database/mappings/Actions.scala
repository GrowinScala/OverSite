package database.mappings

import database.mappings.TablesMysql._

import scala.concurrent.Await
import scala.concurrent.duration._
import slick.jdbc.MySQLProfile.api._


class Actions {

  val db = Database.forConfig("mysql")

  //--------------------------------------Insert into db--------------------------------------------------------------------------

  val insertChat = ChatTable += Chat( "4"  , "Hello World!" )
  val insertEmail = EmailTable += Email( "2"  , "4" , "rvalente@growin.pt","2018-12-02","Hello World!", "How u doin?")

  /**
    * Execute an action to configured db
    * @param action
    * @tparam T
    * @return
    */
  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 50 seconds)
}
