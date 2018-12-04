package dto

import play.api.libs.functional.syntax._
import play.api.libs.json.{Reads, __}
object EmailCreationDTO {

  case class CreateEmailDTO (
                              chatID: String,
                              fromAdress: String,
                              dateOf: String,
                              header: String,
                              body : String
                            )

  implicit val EmailDTOReader: Reads[CreateEmailDTO] =
    (
      (__ \ "chatID").read[String] and
      (__ \ "fromAdress").read[String] and
      (__ \ "dateOf").read[String] and
      (__ \ "header").read[String] and
      (__ \ "body").read[String])(CreateEmailDTO.apply _)
}
