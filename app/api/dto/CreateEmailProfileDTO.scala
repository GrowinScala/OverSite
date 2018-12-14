package api.dto

import com.mysql.cj.xdevapi.JsonValue
import play.api.libs.json._
import play.api.http.ContentTypes
import play.api.http.ContentTypeOf
import play.api.http.Writeable
import play.api.mvc.Codec
import play.libs.F.Tuple

//TODO change to type UUID
case class CreateEmailProfileDTO(
  Id: String,
  header: String)

//case class something(id: UUID, header: String)

object CreateEmailProfileDTO {
  implicit val CreateEmailProfile: OFormat[CreateEmailProfileDTO] = Json.format[CreateEmailProfileDTO]

}
/*
implicit object CreateChatDTOFormat extends Format[CreateChatDTO] {

// convert from Tweet object to JSON (serializing to JSON)
def writes(chatRow: Tuple[String,String]): JsValue = {
  //  tweetSeq == Seq[(String, play.api.libs.json.JsString)]
  val tweetSeq = Seq(
    "chatID" -> JsString(chatRow._1),
    "header" -> JsString(chatRow._2)
    )
  JsObject(tweetSeq)
}

def reads(json: JsonValue): JsResult[CreateChatDTO] = {
JsSuccess(CreateChatDTO(Tuple("", "")))
}
}
*/ 