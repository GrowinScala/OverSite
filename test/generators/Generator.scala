package generators
import definedStrings.testStrings.RepositoryStrings._
import org.scalacheck.Gen
import org.scalacheck.Gen._

import scala.util.Random
class Generator extends CommonDTOGenerator {

  lazy val ID: String = uuid
  lazy val token: String = uuid
  lazy val username: String = emailAddress

  def strGen(max: Int): Gen[String] =
    choose(min = 1, max).flatMap(n =>
      listOfN(n, Gen.alphaNumChar).map(_.mkString))

  /** Generates a random password that varies between 1 and 20 alphanumeric strings*/
  def password: String = strGen(max = 20)

  /** Generates a random email address that varies between 1 and 10 alphanumeric strings + "@growin.pt"*/
  def emailAddress: String = strGen(max = 10) + "@growin.pt"

  /** Creates a sequence of n random email addresses */
  def emailAddressesSeq(num: Int): Seq[String] = {
    if (num == 0) Seq()
    else emailAddressesSeq(num - 1) :+ emailAddress
  }

  /** Generates a Sequence of emailAddresses that varies between 1 and 5**/
  def emailAddresses: Seq[String] = {
    val emailNumberAux: Int = choose(min = 1, max = 5)
    emailAddressesSeq(emailNumberAux)
  }

  /** Generates a string with date format**/
  def dateOf: String = {
    val dateOfYear: Int = choose(min = 1999, max = 2099)
    val dateOfMonth: Int = choose(min = 1, max = 12)
    val dateOfDay: Int = choose(min = 1, max = 29)
    s"$dateOfYear-$dateOfMonth-$dateOfDay"
  }

  /** Shuffle a list of the most common 100 words*/
  def words: List[String] = Random.shuffle(listMostCommonWords)

  /** Generates a string with sentence format, containing around 2 and 5 words */
  def header: String = {
    val headerNumberAux: Int = choose(min = 2, max = 5)
    words.take(headerNumberAux).foldLeft(EmptyString)((string, word) => string + " " + word)
  }

  /** Generates a string with sentence format, containing around 5 and 20 words */
  def body: String = {
    val bodyNumberAux: Int = choose(min = 5, max = 20)
    val bodySentenceAux: List[String] = Random.shuffle(listMostCommonWords).take(bodyNumberAux)
    bodySentenceAux.foldLeft(EmptyString)((string, word) => string + " " + word)
  }

}
