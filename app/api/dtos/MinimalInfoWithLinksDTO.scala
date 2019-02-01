package api.dtos

import play.api.libs.json.{Json, OFormat}

case class MinimalInfoWithLinksDTO(
  Id: String,
  header: String,
  links: List[String])

object MinimalInfoWithLinksDTO {
  implicit val emailMinimalInfoWithLinksDTO: OFormat[MinimalInfoWithLinksDTO] = Json.format[MinimalInfoWithLinksDTO]
}