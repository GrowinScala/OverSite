package definedStrings.testStrings

object RepositoryStrings {

  /**  For status */

  val StatusDraft = "draft"
  val StatusReceived = "received"
  val StatusSent = "sent"
  val ChatID = "123"
  val EmptyChatID = ""
  val DateOf = "2019-01-01"
  val Header = "Hello World"
  val Body = "This body is meant to say hello world"

  val PasswordExample = "banana"
  val WrongTokenExample = "???"
  val WrongPasswordExample = "???"
  val WrongIdExample = "???"
  val RVEmail = "rvalente@growin.com"
  val PLEmail = "pluis@growin.com"
  val PCEmail = "pcorreia@growin.pt"
  val VFEmail = "vfernandes@growin.pt"
  val JPEmail = "jproenca@growin.pt"
  val MREmail = "mreis@growin.pt"

  /** Functions */
  val InsertEmailFunction = " #insertEmail"
  val GetEmailsFunction = " #getEmails"
  val GetEmailFunction = " #getEmail"
  val TakeDraftMakeSent = " #takeDraftMakeSent"
  val InsertChatFunction = " #insertChat"
  val GetInboxFunction = " #getInbox"
  val InsertPermissionFunction = " #insertPermission"
  val GetSharesFunction = " #getShares"
  val GetSharedEmailFunction = " #getSharedEmail"
  val DeletePermissionFunction = " #deletePermission"
  val LoginTableFunction = " #loginTable"
  val InsertUserFunction = " #insertUser"
  val LoginUserFunction = " #loginUser"
  val LogoutUserFunction = " #logoutUser"

  /** For Repositories */
  val EmailRepository = "EmailsController"
  val ChatRepository = "ChatsController"
  val UserRepository = "UsersController"

}
