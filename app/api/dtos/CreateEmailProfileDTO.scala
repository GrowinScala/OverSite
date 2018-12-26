package api.dtos

import play.api.libs.json._

case class CreateEmailProfileDTO(
  Id: String,
  header: String)

object CreateEmailProfileDTO {
  implicit val createEmailProfile: OFormat[CreateEmailProfileDTO] = Json.format[CreateEmailProfileDTO]
}