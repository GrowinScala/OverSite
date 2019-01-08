package api.dtos

import play.api.libs.json._

case class EmailMinimalInfoDTO(
  Id: String,
  header: String)

object EmailMinimalInfoDTO {
  implicit val createEmailProfile: OFormat[EmailMinimalInfoDTO] = Json.format[EmailMinimalInfoDTO]
}