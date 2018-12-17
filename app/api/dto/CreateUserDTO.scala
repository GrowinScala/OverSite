package api.dto

import play.api.libs.json._

case class CreateUserDTO(
  username: String,
  password: String,
)

object CreateUserDTO {
  implicit val UserDTOReader: OFormat[CreateUserDTO] = Json.format[CreateUserDTO]
}