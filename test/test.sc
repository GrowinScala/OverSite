import org.scalacheck.Gen
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalacheck.Gen.{option, posNum}
import org.scalacheck.Gen._


option(strGen(255))
/**

val stringsGen = for {
  alpha <- alphaStr
  num <- numStr
  id <- Gen.identifier
} yield (alpha.take(4), num.take(4), id.take(4))
stringsGen.sample.get

val genVowel = oneOf('a', 'e', 'i', 'o', 'u', 'y')
genVowel.sample.get
//oneOf(choose(-10,-1), choose(1,10))
Gen.alphaNumStr.sample.get.take(choose(1,10).sample.get)*/