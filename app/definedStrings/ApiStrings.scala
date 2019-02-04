package definedStrings

object ApiStrings {

  /**  For end point status */

  val EndPointReceived = "received"
  val EndPointSent = "sent"
  val EndPointTrash = "trashed"
  val EndPointNoFilter = ""
  val EndPointShare = "share"
  val PossibleEndPointStatus = List(EndPointReceived, EndPointSent, EndPointTrash, EndPointNoFilter, EndPointShare)

  val StatusDraft = "draft"
  val StatusTrash = "trash"
  val StatusSend = "send"

  /**  For HTTP error code status */
  val InvalidEmailAddressStatus = "Please insert a valid e-mail address"
  val PasswordMissMatchStatus = "Username and password doesn´t match"
  val MailDraftStatus = "Mail saved as draft"
  val MailSentStatus = "Mail sent"
  val InvalidEndPointStatus = "Invalid status"
  val VerifyLoginStatus = "Please verify your login details \n Try to login again"
  val ImpossibleToSendDraft = "Not possible to send this draft"
  val ImpossibleStatusDraft = "Not possible to execute the intended action"
  val EmailUpdated = "Email updated"

  val SatanStatus = "BURN YOUR LOCAL CHURCH"

  /** For JSON field names */
  val StatusJSONField = "Status"
  val TokenJSONField = "Token"
  val TokenValidTimeJsonField = "Token valid time"

  /** For JSON Header names */
  val TokenHeader = "Token"

  /** For token duration */
  val Token2HourValid = " The token is valid for 2 hour"

  /** For regular strings */
  val ErrorString = "Error:"
  val MessageString = "message"
  val SatanString = "satan"
  val EmptyString = ""
  val IsTrashString = "isTrash"
}
