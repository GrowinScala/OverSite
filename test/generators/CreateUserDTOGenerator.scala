package generators

import api.dtos.CreateUserDTO
import org.scalacheck.Gen
import org.scalacheck.Gen._

class CreateUserDTOGenerator extends CommonDTOGenerator {

  private val username: String = alphaNumStr
  private val numberUsername: Int = choose(1, 10)
  private val password: String = alphaNumStr
  private val numberPassword: Int = choose(1, 20)

  val userDTO = CreateUserDTO(
    username.take(numberUsername) + "@growin.pt",
    password.take(numberPassword))

}
