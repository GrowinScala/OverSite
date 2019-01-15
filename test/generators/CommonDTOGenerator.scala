package generators

import java.util.UUID

import org.scalacheck.Gen

class CommonDTOGenerator {

  implicit def transformAny[T](g: Gen[T]): T = {
    g.sample.get
  }

  implicit def transformEmailUUID(s: Gen[UUID]): String = {
    s.sample.get.toString
  }

}
