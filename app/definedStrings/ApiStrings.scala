package definedStrings

object ApiStrings {

  /**
   * For end point status
   */

  val EndPointDraft = "draft"
  val EndPointReceived = "received"
  val EndPointSent = "sent"
  val PossibleEndPointStatus = List(EndPointDraft, EndPointReceived, EndPointSent)

  /**
   * For HTTP error code status
   */
  val InvalidEmailAddressStatus = "Please insert a valid e-mail address"
  val PasswordMissMatchStatus = "Username and password doesn´t match"
  val MailSentStatus = "Mail sent"
  val InvalidEndPointStatus = "Invalid status"
  val VerifyLoginStatus = "Please verify your login details \n Try to login again"
  val SatanStatus = "BURN YOUR LOCAL CHURCH"

  /**
   * For JSON field names
   */
  val StatusJSONField = "status:"
  val EmailIDJSONField = "Email ID:"
  val ChatIDJSONField = "Chat ID:"
  val FromAddressJSONField = "From address:"
  val ToAddressJSONField = "To address:"
  val HeaderJSONField = "Header:"
  val BodyJSONField = "Body"
  val DateJSONField = "Date:"
  /**
   * For JSON Header names
   */
  val TokenHeader = "Token"

  /**
   * For token duration
   */
  val Token1HourValid = " The token is valid for 1 hour"

  /**
   * For regular strings
   */
  val ErrorString = "Error:"
  val MessageString = "message"
  val SatanString = "satan"
  val EmptyString = ""
}
