package api.dto

import play.api.libs.json._

case class CreateUserDTO(
  username: String,
  password: String,
)

object CreateUserDTO {
  implicit val UserDTO: OFormat[CreateUserDTO] = Json.format[CreateUserDTO]
}