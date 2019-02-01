package api.dtos

import play.api.libs.json.{ Json, OFormat }

case class EmailInfoDTOSenderWithLinks(
  emailID: String,
  chatID: String,
  fromAddress: String,
  username: Seq[String],
  header: String,
  body: String,
  dateOf: String,
  links: List[String])

object EmailInfoDTOSenderWithLinks {
  implicit val emailInfoDTOSenderWithLinks: OFormat[EmailInfoDTOSenderWithLinks] = Json.format[EmailInfoDTOSenderWithLinks]
}