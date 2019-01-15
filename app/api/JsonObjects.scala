package api

import definedStrings.ApiStrings._
import play.api.libs.json._

object JsonObjects {

  def jsonErrors(errors: Seq[(JsPath, Seq[JsonValidationError])]): JsObject = {

    Json.obj(StatusJSONField -> ErrorString, MessageString -> JsError.toJson(errors))
  }
}
