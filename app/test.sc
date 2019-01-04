import java.util.UUID.randomUUID

import api.dtos.CreateEmailDTO
import database.mappings.ChatRow
import database.mappings.ChatMappings.chatTable
import slick.ast.Select
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}
implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration


   def waitToComplete[T](x: Future[T]) = {
    Await.result(x, Duration.Inf)
  }


def help={
  val a =  Future{1+1}
  val b = Future{true || false}.toString
  val c = Future{List(1,9,5,3,2).map(_+1)}


  Future{a + b + c}



}
Await.result(help, Duration.Inf)