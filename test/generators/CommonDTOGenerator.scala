package generators

import org.scalacheck.Gen
import org.scalacheck.Gen._

class CommonDTOGenerator {

  implicit def transformEmail[T](s: Gen[String]): String = {
    s.sample.get
  }
  implicit def transform[T](s: Gen[Int]): Int = {
    s.sample.get
  }
  implicit def transformEmail[T](s: Gen[Option[String]]): Option[String] = {
    s.sample.get
  }
}
