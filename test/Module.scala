import api.validators.{ ProdTokenValidator, TokenValidator }
import com.google.inject.AbstractModule
import database.properties.TestDBProperties
import database.repository._

import scala.concurrent.ExecutionContext

/** Outline the database to be used implicitly */
class Module extends AbstractModule {
  override def configure(): Unit = {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    implicit val tokenValidationTime: Long = 720

    bind(classOf[ChatRepository]).toInstance(new ChatRepositoryImpl(TestDBProperties))
    bind(classOf[UserRepository]).toInstance(new UserRepositoryImpl(TestDBProperties))
    bind(classOf[EmailRepository]).toInstance(new EmailRepositoryImpl(TestDBProperties))
    bind(classOf[TokenValidator]).toInstance(new ProdTokenValidator(TestDBProperties))
  }
}