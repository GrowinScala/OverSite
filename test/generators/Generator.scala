package generators
import org.scalacheck.Gen._
import definedStrings.testStrings.RepositoryStrings._

import scala.util.Random
class Generator extends CommonDTOGenerator {

  lazy val ID: String = uuid
  lazy val token: String = uuid
  lazy val username: String = emailAddress

  def password: String = {
    val passwordAux: String = alphaNumStr
    val numberPassword: Int = choose(1, 20)
    transformEmail(passwordAux).take(numberPassword)
  }

  def emailAddress: String = {
    val emailAddressAux: String = alphaNumStr
    val numberEmailAddress: Int = choose(1, 10)
    emailAddressAux.take(numberEmailAddress) + "@growin.pt"
  }

  def emailAddressesSeq(num: Int): Seq[String] = {

    if (num == 0) Seq()
    else emailAddressesSeq(num - 1) :+ emailAddress
  }

  def emailAddresses: Seq[String] = {
    val emailNumberAux: Int = choose(1, 5)
    emailAddressesSeq(emailNumberAux)
  }

  def dateOf: String = {
    val dateOfYear: Int = choose(1999, 2099)
    val dateOfMonth: Int = choose(1, 12)
    val dateOfDay: Int = choose(1, 29)
    s"$dateOfYear-$dateOfMonth-$dateOfDay"
  }

  def words: List[String] = Random.shuffle(listMostCommonWords)

  def header: String = {
    val headerNumberAux: Int = choose(2, 5)
    words.take(headerNumberAux).foldLeft("")((string, word) => string + " " + word)
  }

  def body: String = {
    val bodyNumberAux: Int = choose(5, 20)
    val bodySentenceAux: List[String] = Random.shuffle(listMostCommonWords).take(bodyNumberAux)
    bodySentenceAux.foldLeft("")((string, word) => string + " " + word)
  }

}
