package api.dtos
import play.api.libs.json._

case class MinimalShareInfoDTO(
  shareID: String,
  username: String,
  header: String)

object MinimalShareInfoDTO {
  implicit val minimalShareInfoDTO: OFormat[MinimalShareInfoDTO] = Json.format[MinimalShareInfoDTO]
}
