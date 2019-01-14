package definedStrings.testStrings

object RepositoryStrings {

  /**  For status */

  val StatusDraft = "draft"
  val StatusReceived = "received"
  val StatusSent = "sent"

  /** Functions */
  val InsertEmailFunction = " #insertEmail"
  val GetEmailsFunction = " #getEmails"
  val GetOneEmailFunction = " #getOneEmail"
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

  val listMostCommonWords = List("a", "about", "all", "also", "and", "as", "at", "be", "because", "but", "by", "can", "come", "could", "day", "do", "even", "find", "first", "for", "from", "get", "give", "go", "have", "he", "her", "here", "him", "his", "how", "I", "if", "in", "into", "it", "its", "just", "know", "like", "look", "make", "man", "many", "me", "more", "my", "new",
    "no", "not", "now", "of", "on", "one", "only", "or", "other", "our", "out", "people", "say", "see", "she", "so", "some", "take", "tell", "than", "that", "the", "their", "them", "then", "there", "these", "they", "thing", "think", "this", "those", "time", "to", "two", "up", "use", "very", "want", "way", "we", "well", "what", "when", "which", "who", "will", "with", "would", "year", "you", "your")
}
