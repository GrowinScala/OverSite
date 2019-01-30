import api.validators.{ ProdTokenValidator, TokenValidator }
import com.google.inject.AbstractModule
import database.properties.{ DatabaseModule, ProdDBProperties, prodModule }
import database.repository._

import scala.concurrent.ExecutionContext

/** Outline the database to be used implicitly */
class Module extends AbstractModule {

  override def configure(): Unit = {

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val profile: DatabaseModule = prodModule
    bind(classOf[DatabaseModule]).toInstance(profile)
    bind(classOf[ChatRepository]).toInstance(new ChatRepositoryImpl(profile, ProdDBProperties))
    bind(classOf[UserRepository]).toInstance(new UserRepositoryImpl(profile, ProdDBProperties))
    bind(classOf[EmailRepository]).toInstance(new EmailRepositoryImpl(profile, ProdDBProperties))
    bind(classOf[TokenValidator]).toInstance(new ProdTokenValidator(profile, ProdDBProperties))
  }
}