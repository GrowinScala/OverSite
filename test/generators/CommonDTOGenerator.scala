package generators

import java.util.UUID

import org.scalacheck.Gen

class CommonDTOGenerator {

  implicit def transformEmail[T](s: Gen[String]): String = {
    s.sample.get
  }
  implicit def transform[T](s: Gen[Int]): Int = {
    s.sample.get
  }
  implicit def transformEmailOption[T](s: Gen[Option[String]]): Option[String] = {
    s.sample.get
  }
  implicit def transformEmailUUID[T](s: Gen[UUID]): String = {
    s.sample.get.toString
  }
}
