package api.dto

import play.api.libs.json._

case class CreateShareDTO(
  chatID: String,
  supervisor: String)

object CreateShareDTO {
  implicit val ShareDTO: OFormat[CreateShareDTO] = Json.format[CreateShareDTO]
}
