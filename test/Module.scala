import api.validators.{ ProdTokenValidator, TokenValidator }
import com.google.inject.AbstractModule
import database.properties.{ DatabaseModule, TestDBProperties, testModule }
import database.repository._

import scala.concurrent.ExecutionContext

/** Outline the database to be used implicitly */
class Module extends AbstractModule {
  override def configure(): Unit = {
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    val profile: DatabaseModule = testModule
    bind(classOf[DatabaseModule]).toInstance(testModule)
    bind(classOf[ChatRepository]).toInstance(new ChatRepositoryImpl(profile, TestDBProperties))
    bind(classOf[UserRepository]).toInstance(new UserRepositoryImpl(profile, TestDBProperties))
    bind(classOf[EmailRepository]).toInstance(new EmailRepositoryImpl(profile, TestDBProperties))
    bind(classOf[TokenValidator]).toInstance(new ProdTokenValidator(profile, TestDBProperties))
  }
}