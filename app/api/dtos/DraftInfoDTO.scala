package api.dtos

import play.api.libs.json._

case class DraftInfoDTO(

  draftID: String,
  username: String,
  toAddresses: Seq[String],
  ccs: Seq[String],
  bccs: Seq[String],
  header: String,
  body: String,
  dateOf: String)

object DraftInfoDTO {
  implicit val draftInfoDTO: OFormat[DraftInfoDTO] = Json.format[DraftInfoDTO]
}
