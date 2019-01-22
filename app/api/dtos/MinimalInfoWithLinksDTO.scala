package api.dtos

import play.api.libs.json._

case class MinimalInfoWithLinksDTO(
  Id: String,
  header: String,
  links: List[String])

object MinimalInfoWithLinksDTO {
  implicit val emailMinimalInfoWithLinksDTO: OFormat[MinimalInfoWithLinksDTO] = Json.format[MinimalInfoWithLinksDTO]
}