import api.validators.{ ProdTokenValidator, TokenValidator }
import com.google.inject.AbstractModule
import database.properties.ProdDBProperties
import database.repository._

import scala.concurrent.ExecutionContext

/** Outline the database to be used implicitly */
class Module extends AbstractModule {
  //val tokenValidator = new TokenValidator(dbObject)

  override def configure(): Unit = {

    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    //implicit val dbclass: dbClass = prodDb
    //bind(classOf[dbClass]).toInstance(prodDb)
    bind(classOf[ChatRepository]).toInstance(new ChatRepositoryImpl(ProdDBProperties))
    bind(classOf[UserRepository]).toInstance(new UserRepositoryImpl(ProdDBProperties))
    bind(classOf[EmailRepository]).toInstance(new EmailRepositoryImpl(ProdDBProperties))
    bind(classOf[TokenValidator]).toInstance(new ProdTokenValidator(ProdDBProperties.db))
  }
}