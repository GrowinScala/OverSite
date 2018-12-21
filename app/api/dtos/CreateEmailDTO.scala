package api.dtos

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
  implicit val emailDTO: OFormat[CreateEmailDTO] = Json.format[CreateEmailDTO]
}