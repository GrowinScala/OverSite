package api.dtos

import play.api.libs.json.{ Json, OFormat }

case class EmailInfoDTOSender(
  emailID: String,
  chatID: String,
  fromAddress: String,
  username: String,
  header: String,
  body: String,
  dateOf: String)

object EmailInfoDTOSender {
  implicit val emailInfoDTOSender: OFormat[EmailInfoDTOSender] = Json.format[EmailInfoDTOSender]
}