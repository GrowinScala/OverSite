package api.dto

import play.api.libs.json._

case class CreateEmailDTO(
  chatID: Option[String],
  fromAddress: String,
  dateOf: String,
  header: String,
  body: String,
  to: Option[Seq[String]],
  BCC: Option[Seq[String]],
  CC: Option[Seq[String]],
  sendNow: Boolean)

object EmailCreationDTO {
  implicit val EmailDTOReader: OFormat[CreateEmailDTO] = Json.format[CreateEmailDTO]
}