package regex

import scala.util.matching.Regex

/** Regex pattern to detect if string has the e-mail structure*/
object RegexPatterns {
  lazy val emailAddressPattern = new Regex("\\w+\\@\\w+\\.(pt|com)$")
}
