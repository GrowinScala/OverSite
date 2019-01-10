package generators
import org.scalacheck.Gen._
import definedStrings.testStrings.RepositoryStrings._

import scala.util.Random
class Generator extends CommonDTOGenerator {

  lazy val ID: String = uuid
  lazy val token: String = uuid
  lazy val username: String = emailAddress

  /** Generates a random password that varies between 1 and 20 alphanumeric strings*/
  def password: String = {
    val passwordAux: String = alphaNumStr
    val numberPassword: Int = choose(1, 20)
    transformEmail(passwordAux).take(numberPassword)
  }

  /** Generates a random email address that varies between 1 and 10 alphanumeric strings + "@growin.pt"*/
  def emailAddress: String = {
    val emailAddressAux: String = alphaNumStr
    val numberEmailAddress: Int = choose(1, 10)
    emailAddressAux.take(numberEmailAddress) + "@growin.pt"
  }

  /** Creates a sequence of n random email addresses */
  def emailAddressesSeq(num: Int): Seq[String] = {
    if (num == 0) Seq()
    else emailAddressesSeq(num - 1) :+ emailAddress
  }

  /** Generates a Sequence of emailAddresses that varies between 1 and 5**/
  def emailAddresses: Seq[String] = {
    val emailNumberAux: Int = choose(1, 5)
    emailAddressesSeq(emailNumberAux)
  }

  /** Generates a string with date format**/
  def dateOf: String = {
    val dateOfYear: Int = choose(1999, 2099)
    val dateOfMonth: Int = choose(1, 12)
    val dateOfDay: Int = choose(1, 29)
    s"$dateOfYear-$dateOfMonth-$dateOfDay"
  }

  /** Shuffle a list of the most common 100 words*/
  def words: List[String] = Random.shuffle(listMostCommonWords)

  /** Generates a string with sentence format, containing around 2 and 5 words */
  def header: String = {
    val headerNumberAux: Int = choose(2, 5)
    words.take(headerNumberAux).foldLeft("")((string, word) => string + " " + word)
  }

  /** Generates a string with sentence format, containing around 5 and 20 words */
  def body: String = {
    val bodyNumberAux: Int = choose(5, 20)
    val bodySentenceAux: List[String] = Random.shuffle(listMostCommonWords).take(bodyNumberAux)
    bodySentenceAux.foldLeft("")((string, word) => string + " " + word)
  }

}
