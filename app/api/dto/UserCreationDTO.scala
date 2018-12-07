package api.dto

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CreateUserDTO(
                          username: String,
                          password: String,
                        )

object UserCreationDTO {
  implicit val UserDTOReader: OFormat[CreateUserDTO] = Json.format[CreateUserDTO]
}