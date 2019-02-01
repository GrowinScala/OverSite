package api.dtos

import play.api.libs.json.{ Json, OFormat }

case class DraftStatusDTO(
  status: String)

object DraftStatusDTO {
  implicit val draftStatusDTO: OFormat[DraftStatusDTO] = Json.format[DraftStatusDTO]
}