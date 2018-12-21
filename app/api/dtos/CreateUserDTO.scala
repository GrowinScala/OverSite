package api.dtos

import play.api.libs.json._

case class CreateUserDTO(
  username: String,
  password: String,
)

object CreateUserDTO {
  implicit val userDTOReader: OFormat[CreateUserDTO] = Json.format[CreateUserDTO]
}