package api.dtos

import play.api.libs.json._

case class MinimalInfoDTO(
  Id: String,
  header: String)

object MinimalInfoDTO {
  implicit val createEmailProfile: OFormat[MinimalInfoDTO] = Json.format[MinimalInfoDTO]

  def addLink(minimalInfoDTO: MinimalInfoDTO, link: List[String]): MinimalInfoWithLinksDTO = {
    MinimalInfoWithLinksDTO(minimalInfoDTO.Id, minimalInfoDTO.header, link)
  }
}