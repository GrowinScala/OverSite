import org.scalacheck.Gen
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalacheck.Gen.{option, posNum}
import org.scalacheck.Gen._
import api.controllers._

"http://localhost:9000" + routes.UsersController.logIn().absoluteURL()