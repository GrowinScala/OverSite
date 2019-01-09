package definedStrings.testStrings

object ControllerStrings {

  /**  For status */

  val StatusDraft = "draft"
  val StatusReceived = "received"
  val StatusSent = "sent"

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

  /** For end-points */

  val EmailEndpointRoute = "/email"
  val EmailsEndpointRoute = "/emails/"
  val ChatsEndpointRoute = "/chats"
  val SharesEndpointRoute = "/shares"
  val SignInEndpointRoute = "/signin"
  val LogInEndpointRoute = "/login"
  val LogOutEndpointRoute = "/logout"

  val Emails = "/emails"
  val StatusUndefined = ":status"
  val EmailIDUndefined = ":emailID"
  val ChatIDUndefined = ":chatID"
  val ShareIDUndefined = ":shareID"

  /** For Controllers */

  val EmailsController = "EmailsController"
  val ChatsController = "ChatsController"
  val UsersController = "UsersController"

  /** For examples */

  val LocalHost = "localhost:9000"
  val TokenKey = "Token"

  val EmailExample = "pedro@hotmail.com"
  val InvalidEmailExample = "pedro@hotmail"

  val PasswordExample = "12345"
  val WrongPasswordExample = "???"
  //encrypted "12345" password
  val EncryptedPasswordExample = "13012420314234138112108765216110414524878123"

  val TokenExample1 = "9e2907a7-b939-4b33-8899-6741e6054822"
  val TokenExample2 = "b93907a7-b939-4b33-8899-6741e6054822"
  val WrongTokenExample = "???"
  val EmailIDExample = "1ba62fff-f787-4d19-926c-1ba62fd03a9a"
  val ChatIDExample = "6e9601ff-f787-4d19-926c-1ba62fd03a9a"
  /*
  * "chatID": "6e9601ff-f787-4d19-926c-1ba62fd03a9a",
            "NOTdateOf": "2018-12-01",
            "header": "Hello World!",
            "body": "Have a good day Sir",
            "to": ["vfernandes@growin.pt"],
            "BCC": ["rvalente@growin.pt"],
            "CC": ["joao@growin.pt"],
            "sendNow"
  * */

  val UsernameKey = "username"
  val PasswordKey = "password"
  val ChatIDKey = "chatID"
  val SupervisorKey = "supervisor"
  val DateOfKey = "dateOf"
  val HeaderKey = "header"
  val BodyKey = "body"
  val ToKey = "to"
  val BCCKey = "BCC"
  val CCKey = "CC"
  val SendNowKey = "sendNow"

  val WrongUsernameKey = "NOTusername"
  val WrongPasswordKey = "NOTpassword"
  val WrongChatIDKey = "NOTchatID"
  val WrongSupervisorKey = "NOTsupervisor"
  val WrongDateOfKey = "NOTdateOf"
  val WrongHeaderKey = "NOTheader"
  val WrongBodyKey = "NOTbody"
  val WrongToKey = "NOTto"
  val WrongBCCKey = "NOTBCC"
  val WrongCCKey = "NOTCC"
  val WrongSendNowKey = "NOTsendNow"

  /** For error messages */

  val InvalidJSONBodyBadRequest = "send a BadRequest if JSON body has an invalid format:"
  val ValidJSONBodyOk = "send an OK if JSON body has an valid format:"
  val InvalidTokenForbidden = "send a Forbidden if JSON header has an invalid token"
  val ValidTokenOk = "send a OK if JSON header has a valid token"
  val InvalidStatusBadRequest = "send a BadRequest if end-point has an invalid status"
  val HasNoToAddressBadRequest = "send a BadRequest if target email has no to address"
  val InvalidEmailAddress = "send a BadRequest if username is not a valid email address"
  val ValidJSONBodyCreated = "send a Created if JSON body has a valid format "
  val MissMatchPasswordForbidden = "send a Forbidden if username and password doesn't match"
  val AlreadyLoggedOutForbidden = "send a Forbidden if JSON header has a valid token but the user is already log out"

  val AndJsonBody = " and a valid JSON body"
  val AndStatus = " and status: "
  val AndHasToAddress = "and target email has to address"

  /** */

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
