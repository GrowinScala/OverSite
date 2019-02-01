package api.dtos

import play.api.libs.json.{ Json, OFormat }

case class EmailInfoDTO(

  chatID: String,
  fromAddress: String,
  username: Seq[String],
  header: String,
  body: String,
  dateOf: String)

object EmailInfoDTO {
  implicit val emailInfoDTO: OFormat[EmailInfoDTO] = Json.format[EmailInfoDTO]
}