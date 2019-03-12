import api.validators.{ ProdTokenValidator, TokenValidator }
import com.google.inject.AbstractModule
import com.typesafe.config.ConfigFactory
import database.properties.ProdDBProperties
import database.repository._

import scala.concurrent.ExecutionContext

/** Outline the database to be used implicitly */
class Module extends AbstractModule {

  override def configure(): Unit = {

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    implicit val tokenValidationTime: Long = ConfigFactory.load().getLong("tokenValidationTime")

    bind(classOf[ChatRepository]).toInstance(new ChatRepositoryImpl(ProdDBProperties))
    bind(classOf[UserRepository]).toInstance(new UserRepositoryImpl(ProdDBProperties))
    bind(classOf[EmailRepository]).toInstance(new EmailRepositoryImpl(ProdDBProperties))
    bind(classOf[TokenValidator]).toInstance(new ProdTokenValidator(ProdDBProperties))
  }
}