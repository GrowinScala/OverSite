package api.dto

import play.api.libs.functional.syntax._
import play.api.libs.json.{ OWrites, Reads, __ }

object EmailCreationDTO {

  case class CreateEmailDTO(
    chatID: String,
    fromAddress: String,
    dateOf: String,
    header: String,
    body: String,
    to: Seq[String],
    BCC: Seq[String],
    CC: Seq[String],
    sendNow: Boolean)

  implicit val EmailDTOReader: Reads[CreateEmailDTO] =
    (
      (__ \ "chatID").read[String] and
      (__ \ "fromAddress").read[String] and
      (__ \ "dateOf").read[String] and
      (__ \ "header").read[String] and
      (__ \ "body").read[String] and
      (__ \ "to").read[Seq[String]] and
      (__ \ "BCC").read[Seq[String]] and
      (__ \ "CC").read[Seq[String]] and
      (__ \ "sendNow").read[Boolean])(CreateEmailDTO.apply _)

  implicit val EmailDTOWriter: OWrites[CreateEmailDTO] =
    (
      (__ \ "chatID").write[String] and
      (__ \ "fromAddress").write[String] and
      (__ \ "dateOf").write[String] and
      (__ \ "header").write[String] and
      (__ \ "body").write[String] and
      (__ \ "to").write[Seq[String]] and
      (__ \ "BCC").write[Seq[String]] and
      (__ \ "CC").write[Seq[String]] and
      (__ \ "sendNow").write[Boolean])(unlift(CreateEmailDTO.unapply))

}
