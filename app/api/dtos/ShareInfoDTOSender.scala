package api.dtos

import play.api.libs.json.{Json, OFormat}

case class ShareInfoDTOSender(
  shareId: String,
  emailID: String,
  chatID: String,
  fromAddress: String,
  username: Seq[String],
  header: String,
  body: String,
  dateOf: String)

object ShareInfoDTOSender {
  implicit val emailInfoDTOSender: OFormat[ShareInfoDTOSender] = Json.format[ShareInfoDTOSender]
}