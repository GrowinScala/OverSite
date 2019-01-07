package definedStrings.testStrings

object ControllerStrings {

  /**  For status */

  val StatusDraft = "draft"
  val StatusReceived = "received"
  val StatusSent = "sent"

  val StatusUndefined = ":status"
  val EmailIDUndefined = ":emailID"

  /** For Functions */

  val EmailFunction = "#email"
  val GetEmailsFunction = "#getEmails"
  val GetEmailFunction = "#getEmail"
  val ToSentFunction = "#toSent"

  val EmailEndpointRoute = s"/email"
  val EmailsEndpointRoute = s"/emails/"

  /** For Controller */

  val EmailsController = "EmailsController"
  val ChatsController = "ChatsController"
  val UsersController = "UsersController"

  val LocalHost = "localhost:9000"
  val TokenKey = "Token"

  val EmailExample1 = "pedro@hotmail.com"
  //encrypted "12345" password
  val EncryptedPasswordExample = "13012420314234138112108765216110414524878123"

  val TokenExample = "9e2907a7-b939-4b33-8899-6741e6054822"
  val WrongTokenExample = "???"
  val EmailIDExample = "1ba62fff-f787-4d19-926c-1ba62fd03a9a"

  val InvalidJSONBodyBadRequest = "send a BadRequest if JSON body has an invalid format:"
  val ValidJSONBodyOk = "send an OK if JSON body has an valid format:"
  val InvalidTokenForbidden = "send a Forbidden if JSON header has an invalid token"
  val ValidTokenOk = "send a OK if JSON header has a valid token"
  val InvalidStatusBadRequest = "send a BadRequest if end-point has an invalid status"
  val HasNoToAddressBadRequest = "send a BadRequest if target email has no to address"

  val AndJsonBody = " and a valid JSON body"
  val AndStatus = " and status: "
  val AndHasToAddress = "and target email has to address"

  val CaseDateOf = " case dateOf"
  val CaseHeader = " case header"
  val CaseBody = " case body"
  val CaseSendNow = " case sendNow"

  val CaseMissingChatID = " case missing parameter chatID"
  val CaseMissingDateOf = " case missing parameter dateOf"
  val CaseMissingHeader = " case missing parameter header"
  val CaseMissingBody = " case missing parameter body"
  val CaseMissingTo = " case missing parameter to"
  val CaseMissingBCC = " case missing parameter BCC"
  val CaseMissingCC = " case missing parameter CC"
  val CaseMissingSendNow = " case missing parameter sendNow"
}
