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


   val x = Seq().zip(Seq())
println(x.map(r => r._1))
