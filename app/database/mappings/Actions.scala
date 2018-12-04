package database.mappings
/*
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.Await
import scala.concurrent.duration._
import database.mappings.ChatObject._
import database.mappings.EmailObject._

class Actions {

  val db = Database.forConfig("mysql")

  //--------------------------------------Insert into db--------------------------------------------------------------------------

  val insertChat = ChatTable += Chat( "4"  , "Hello World!" )
  def insertEmail(email:Email) = EmailTable += email

  /**
    * Execute an action to configured db
    * @param action
    * @tparam T
    * @return
    */
  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)
}
*/
