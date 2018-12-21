import java.util.UUID.randomUUID

import api.dtos.CreateEmailDTO
import database.mappings.ChatRow
import database.mappings.ChatMappings.chatTable
import slick.ast.Select
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext
import scala.util.matching.Regex
import regex.RegexPatterns._
"fasd@cacsd.pt".replaceAll("\\w+\\@\\w+\\.(pt|com)$","").isEmpty
emailAddressPattern.findAllIn("fasd@cacsd.pt").nonEmpty

def isEmailAddress(regexPattern:Regex, possibleEmailAddress:String)={
  regexPattern.findAllIn(possibleEmailAddress).nonEmpty
}
List("adeus@hotmail.co","hihi@hotmail.com","lolol@hotmail.com","ola@hotmail.com").forall(isEmailAddress(emailAddressPattern,_))