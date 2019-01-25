package definedStrings.testStrings

object ControllerStrings {

  /**  For status */

  val StatusDraft = "draft"
  val StatusReceived = "received"
  val StatusSent = "sent"

  val OptionalStatus = "?status="

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
  val MoveInOutTrashFunction = "#moveInOutTrash"
  val UpdateDraftFunction = "#updateDraft"

  /** For end-points */

  val EmailEndpointRoute = "/email"
  val EmailsEndpointRoute = "/emails/"
  val ChatsEndpointRoute = "/chats"
  val WithOptionTrue = "?isTrash=true"
  val SharesEndpointRoute = "/shares"
  val SignInEndpointRoute = "/signin"
  val LogInEndpointRoute = "/login"
  val LogOutEndpointRoute = "/logout"
  val EndpointPatchSendStatus = "/send"
  val EndpointPatchTrashStatus = "/trash"
  val EndpointPatchUpdateStatus = "/update"

  val Emails = "/emails"
  val StatusUndefined = ":status"
  val EmailIDUndefined = ":emailID"
  val ChatIDUndefined = ":chatID"
  val ShareIDUndefined = ":shareID"

  /** For Controllers */

  val EmailsController = "EmailsController"
  val ChatsController = "ChatsController"
  val UsersController = "UsersController"

  val LocalHost = "localhost:9000"

  /** For Json keys */

  val TokenKey = "Token"
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
  val TrueKey = "true"

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
  val ToTrashKey = "toTrash"

  /** For error messages */

  val InvalidJSONBodyBadRequest = "send a BadRequest if JSON body has an invalid format:"
  val ValidJSONBodyOk = "send an OK if JSON body has an valid format:"
  val InvalidTokenForbidden = "send a Forbidden if JSON header has an invalid token"
  val ValidTokenOk = "send a OK if JSON header has a valid token"
  val ValidTokenOkWithOption = "send a OK if JSON header has a valid token and a valid body"
  val InvalidStatusBadRequest = "send a BadRequest if end-point has an invalid status"
  val HasNoToAddressBadRequest = "send a BadRequest if target email has no to address"
  val InvalidEmailAddress = "send a BadRequest if username is not a valid email address"
  val ValidJSONBodyCreated = "send a Created if JSON body has a valid format "
  val MissMatchPasswordForbidden = "send a Forbidden if username and password doesn't match"
  val PasswordMatchOk = "send an Ok if username and password match"
  val AlreadyLoggedOutForbidden = "send a Forbidden if JSON header has a valid token but the user is already log out"
  val UndefinedStatusOk = "Ok with undefined status"

  val AndJsonBody = " and a valid JSON body"
  val AndStatus = " and status: "
  val AndHasToAddress = " and target email has to address"

  /** For test cases */

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
  val CaseEmptyStatus = "case empty optionalStatus = "

  val CaseUpdateStatus = " update status"
  val CaseTrashStatus = " trash status"
  val CaseSendStatus = " send status"
}
