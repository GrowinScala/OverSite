package api.validators

import definedStrings.ApiStrings._

import scala.concurrent.Future

/** Mocked Token Validator to be used in Testing */
class MockedInvalidTokenValidator extends TokenValidator {
  /**
   * Validates the userName and token inserted by the user
   *
   * @param token token provided from the headers
   * @return boolean value considering of the token is valid or not
   */
  def validateToken(token: String): Future[Boolean] = {
    Future.successful(false)
  }

  /**
   * Corresponds an token to an username
   *
   * @param token token provided from the headers
   * @return Username associated to token
   */
  def getUserByToken(token: String): Future[String] = {
    Future.successful(EmptyString)
  }
}

