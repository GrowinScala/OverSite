import java.util.UUID.randomUUID

import api.dto.CreateEmailDTO
import database.mappings.Chat
import database.mappings.ChatMappings.ChatTable
import slick.ast.Select
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext

/*
List((Content-Length,223), (Content-Type,application/json), (Remote-Address,0:0:0:0:0:0:0:1:62243), (Raw-Request-URI,/users/vfernandes@growin.pt/email),
(Tls-S ession-Info,[Session-1, SSL_NULL_WITH_NULL_NULL]), (Token,hellostranger),
(Cache-Control,no-cache), (Postman-Token,37a08198-d19b-4c0d-bab6-0b42d69e43da),
(User -Agent,PostmanRuntime/7.4.0),
(Accept,* /*),
(Host,localhost:9000),
(accept-encoding,gzip, deflate),
(Connection,keep-alive),
(Timeout-Access,<function1>))*/