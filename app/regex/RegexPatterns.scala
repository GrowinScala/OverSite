package regex

import scala.util.matching.Regex

object RegexPatterns {
  val emailAddressPattern = new Regex("\\w+\\@\\w+\\.(pt|com)$")
}
