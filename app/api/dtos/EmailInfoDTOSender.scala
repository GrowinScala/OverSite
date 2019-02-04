package api.dtos

import play.api.libs.json.{ Json, OFormat }

case class EmailInfoDTOSender(
  emailID: String,
  chatID: String,
  fromAddress: String,
  username: Seq[String],
  header: String,
  body: String,
  dateOf: String)

object EmailInfoDTOSender {
  implicit val emailInfoDTOSender: OFormat[EmailInfoDTOSender] = Json.format[EmailInfoDTOSender]

  def addLink(emailInfoDTOSender: EmailInfoDTOSender, link: List[String]): EmailInfoDTOSenderWithLinks = {
    EmailInfoDTOSenderWithLinks(
      emailInfoDTOSender.emailID,
      emailInfoDTOSender.chatID,
      emailInfoDTOSender.fromAddress,
      emailInfoDTOSender.username,
      emailInfoDTOSender.header,
      emailInfoDTOSender.body,
      emailInfoDTOSender.dateOf,
      link)
  }
}