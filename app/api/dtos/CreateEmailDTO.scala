package api.dtos

import play.api.libs.json._
import slick.ast.Type

import scala.util.matching.Regex

case class CreateEmailDTO(

  chatID: Option[String],
  dateOf: String,
  header: String,
  body: String,
  to: Option[Seq[String]],
  BCC: Option[Seq[String]],
  CC: Option[Seq[String]],
  sendNow: Boolean)

object CreateEmailDTO {
  type emailAddress = Regex
  implicit val emailDTO: OFormat[CreateEmailDTO] = Json.format[CreateEmailDTO]
}

//"\\w+\\@\\w+\\.(pt|com)$"