package api.dtos

import play.api.libs.json._

case class EmailInfoDTO(

  chatID: String,
  fromAddress: String,
  username: String,
  header: String,
  body: String,
  dateOf: String)

object EmailInfoDTO {
  implicit val emailInfoDTO: OFormat[EmailInfoDTO] = Json.format[EmailInfoDTO]
}