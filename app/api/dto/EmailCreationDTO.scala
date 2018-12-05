package api.dto

import play.api.libs.functional.syntax._
import play.api.libs.json.{ OWrites, Reads, __ }

object EmailCreationDTO {
  case class CreateEmailDTO(
    chatID: String,
    fromAddress: String,
    dateOf: String,
    header: String,
    body: String)

  implicit val EmailDTOReader: Reads[CreateEmailDTO] =
    (
      (__ \ "chatID").read[String] and
      (__ \ "fromAddress").read[String] and
      (__ \ "dateOf").read[String] and
      (__ \ "header").read[String] and
      (__ \ "body").read[String])(CreateEmailDTO.apply _)

  implicit val EmailDTOWriter: OWrites[CreateEmailDTO] =
    (
      (__ \ "chatID").write[String] and
      (__ \ "fromAddress").write[String] and
      (__ \ "dateOf").write[String] and
      (__ \ "header").write[String] and
      (__ \ "body").write[String])(unlift(CreateEmailDTO.unapply))

}
