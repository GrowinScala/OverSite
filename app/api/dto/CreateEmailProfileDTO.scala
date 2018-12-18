package api.dto

import play.api.libs.json._

//TODO change to type UUID
case class CreateEmailProfileDTO(
  Id: String,
  header: String)

object CreateEmailProfileDTO {
  implicit val CreateEmailProfile: OFormat[CreateEmailProfileDTO] = Json.format[CreateEmailProfileDTO]

}