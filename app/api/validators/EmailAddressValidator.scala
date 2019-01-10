package api.validators

import regex.RegexPatterns.emailAddressPattern

import scala.util.matching.Regex

object EmailAddressValidator {

  /** Checks if target string is a valid email address */
  def isEmailAddress(regexPattern: Regex, possibleEmailAddress: String): Boolean = {
    regexPattern.findAllIn(possibleEmailAddress).nonEmpty
  }

  /** Checks if a list of target strings are all email addresses */
  def validateEmailAddress(regexPattern: Regex, possibleEmailAddress: Either[String, List[String]]): Boolean = {

    possibleEmailAddress match {
      case Left(value) => isEmailAddress(regexPattern, value)

      case Right(value) => value.forall(isEmailAddress(emailAddressPattern, _))
    }
  }
}
