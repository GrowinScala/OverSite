package api.dtos

import play.api.libs.json.{ Json, OFormat }

case class TrashInfoDTO(
  toTrash: Boolean)

object TrashInfoDTO {
  implicit val trashInfoDTO: OFormat[TrashInfoDTO] = Json.format[TrashInfoDTO]
}