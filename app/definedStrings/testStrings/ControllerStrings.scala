package definedStrings.testStrings

object ControllerStrings {

  /**  For status */

  val StatusDraft = "draft"
  val StatusReceived = "received"
  val StatusSent = "sent"

  val Emails = "/emails"
  val StatusUndefined = ":status"
  val EmailIDUndefined = ":emailID"
  val ChatIDUndefined = ":chatID"
  val ShareIDUndefined = ":shareID"

  /** For Functions */

  val EmailFunction = "#email"
  val GetEmailsFunction = "#getEmails"
  val GetEmailFunction = "#getEmail"
  val ToSentFunction = "#toSent"
  val InboxFunction = "#inbox"
  val SupervisedFunction = "#supervised"
  val SharesFunction = "#getShares"
  val GetSharedEmailsFunction = "#getSharedEmails"
  val GetSharedEmailFunction = "#getSharedEmail"
  val TakePermissionsFunction = "#takePermissions"
  val SignInFunction = "#signIn"
  val LoginFunction = "#logIn"
  val LogoutFunction = "#logOut"

  val EmailEndpointRoute = s"/email"
  val EmailsEndpointRoute = s"/emails/"
  val ChatsEndpointRoute = s"/chats"
  val SharesEndpointRoute = s"/shares"
  val SignInEndpointRoute = s"/signin"
  val LogInEndpointRoute =  s"/login"
  val LogOutEndpointRoute = s"/logout"

  /** For Controller */

  val EmailsController = "EmailsController"
  val ChatsController = "ChatsController"
  val UsersController = "UsersController"

  val LocalHost = "localhost:9000"
  val TokenKey = "Token"

  val EmailExample = "pedro@hotmail.com"
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
  val InvalidEmailAddress = "send a BadRequest if username is not a valid email address"
  val ValidJSONBodyCreated =  "send a Created if JSON body has a valid format "
  val MissMatchPasswordForbidden = "send a Forbidden if username and password doesn't match"
  val AlreadyLoggedOutForbidden =  "send a Forbidden if JSON header has a valid token but the user is already log out"

  val AndJsonBody = " and a valid JSON body"
  val AndStatus = " and status: "
  val AndHasToAddress = "and target email has to address"

  val CaseDateOf = " case dateOf"
  val CaseHeader = " case header"
  val CaseBody = " case body"
  val CaseSendNow = " case sendNow"
  val CaseChatID = " case chatID"
  val CaseSupervisor = " case supervisor"
  val CaseUsername = " case username"
  val CasePassword = " case password"

  val CaseMissingChatID = " case missing parameter chatID"
  val CaseMissingDateOf = " case missing parameter dateOf"
  val CaseMissingHeader = " case missing parameter header"
  val CaseMissingBody = " case missing parameter body"
  val CaseMissingTo = " case missing parameter to"
  val CaseMissingBCC = " case missing parameter BCC"
  val CaseMissingCC = " case missing parameter CC"
  val CaseMissingSendNow = " case missing parameter sendNow"
  val CaseMissingSupervisor = " case missing supervisor parameter"
  val CaseMissingPassword = " case missing password parameter"
  val CaseMissingUsername = " case missing username parameter"
}
