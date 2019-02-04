package definedStrings

object DatabaseStrings {

  /** For table names */
  val ChatsTable = "chats"
  val SharesTable = "shares"
  val EmailsTable = "emails"
  val DraftsTable = "drafts"
  val DraftsDestinationTable = "draftsdestination"
  val EmailsDestinationTable = "emailsdestination"
  val ToValue = "TO"
  val CCValue = "CC"
  val BCCValue = "BCC"
  val LoginsTable = "logins"
  val UsersTable = "users"

  /** For row names */
  val ChatIDRow = "chatID"
  val HeaderRow = "header"
  val UsernameRow = "username"
  val ShareIDRow = "shareID"
  val FromUserRow = "fromUser"
  val ToUserRow = "toUser"
  val EmailIDRow = "emailID"
  val DraftIDRow = "draftID"
  val DestinationRow = "destination"
  val FromAddressRow = "fromAddress"
  val DateOfRow = "dateOf"
  val BodyRow = "body"
  val TrashRow = "trash"
  val TokenRow = "token"
  val ValidDateRow = "validDate"
  val ActiveRow = "active"
  val PasswordRow = "password"

  /** For database config names */
  val OversiteDB = "oversiteDB"

  /** For regular strings */
  val EmptyString = ""
  val TripletEmptyString = (EmptyString, EmptyString, EmptyString)
}
