package api.dtos

import play.api.libs.json._

case class TrashInfoDTO(
  toTrash: Boolean)

object TrashInfoDTO {
  implicit val trashInfoDTO: OFormat[TrashInfoDTO] = Json.format[TrashInfoDTO]
}