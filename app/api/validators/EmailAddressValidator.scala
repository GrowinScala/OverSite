package api.validators

import regex.RegexPatterns.emailAddressPattern

import scala.util.matching.Regex

object EmailAddressValidator {

  def isEmailAddress(regexPattern: Regex, possibleEmailAddress: String): Boolean = {
    regexPattern.findAllIn(possibleEmailAddress).nonEmpty
  }

  def validateEmailAddress(regexPattern: Regex, possibleEmailAddress: Either[String, List[String]]): Boolean = {

    possibleEmailAddress match {
      case Left(value) => isEmailAddress(regexPattern, value)

      case Right(value) => value.forall(isEmailAddress(emailAddressPattern, _))
    }
  }
}
