package api.dto

import play.api.libs.functional.syntax._
import play.api.libs.json.{OWrites, Reads, __}

object UserCreationDTO {

  case class CreateUserDTO(
    username: String,
    password: String,
  )

  implicit val EmailDTOReader: Reads[CreateUserDTO] =
    (
      (__ \ "username").read[String] and
      (   __ \ "password").read[String]
    )(CreateUserDTO.apply _)

}
