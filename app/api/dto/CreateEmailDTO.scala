package api.dto

import play.api.libs.json._

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
  implicit val EmailDTO: OFormat[CreateEmailDTO] = Json.format[CreateEmailDTO]
}