package api.dtos

import play.api.libs.json.{Json, OFormat}

case class CreateShareDTO(
  chatID: String,
  supervisor: String)

object CreateShareDTO {
  implicit val shareDTO: OFormat[CreateShareDTO] = Json.format[CreateShareDTO]
}