package api.dtos

import play.api.libs.json._

case class EmailMinimalInfoWithLinksDTO(
  Id: String,
  header: String,
  links: List[String])

object EmailMinimalInfoWithLinksDTO {
  implicit val emailMinimalInfoWithLinksDTO: OFormat[EmailMinimalInfoWithLinksDTO] = Json.format[EmailMinimalInfoWithLinksDTO]
}