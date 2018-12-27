import akka.actor.ActorSystem
import akka.stream.Materializer
import api.controllers.ChatController
import api.validators.TokenValidator
import database.repository.{ ChatRepository, EmailRepository, UserRepository }
import javax.inject.Inject
import org.scalatestplus.play._
import play.api.mvc._
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext

class UsersControllerTest extends PlaySpec with Results {

  "Example Page#signIn" should {
    "should be valid" in {
      assert("ola" === "ok")
    }
  }
}