package api.dtos

import play.api.libs.json.{Json, OFormat}

case class CreateEmailDTO(

  chatID: Option[String],
  dateOf: String,
  header: String,
  body: String,
  to: Option[Seq[String]],
  BCC: Option[Seq[String]],
  CC: Option[Seq[String]])

object CreateEmailDTO {
  implicit val emailDTO: OFormat[CreateEmailDTO] = Json.format[CreateEmailDTO]
}