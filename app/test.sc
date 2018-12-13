import java.util.UUID.randomUUID

import api.dto.CreateEmailDTO
import database.mappings.Chat
import database.mappings.ChatMappings.ChatTable
import slick.ast.Select
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext


val db = Database.forConfig()forConfig("mysql")/*
val x = sqlu"""Select * from ChatTable """
db.run(x)