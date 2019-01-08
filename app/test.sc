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


EmailInfoDTO(8a901ddd-cef1-4471-b751-01569fe5bdef,rvalente@growin.com,,Hello World,This body is meant to say hello world,2025-10-10)
did not equal (8a901ddd-cef1-4471-b751-01569fe5bdef,rvalente@growin.com,,Hello World,This body is meant to say hello world,2025-10-10)