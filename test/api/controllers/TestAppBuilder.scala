package api.controllers

import akka.actor.ActorSystem
import akka.stream.Materializer
import api.validators.{ MockedTokenValidator, TokenValidator }
import database.repository._
import javax.inject.Singleton
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ Injector, bind }
import play.api.mvc.ControllerComponents

@Singleton
object TestAppBuilder {
  val appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
    .load(
      new play.api.inject.BuiltinModule,
      new play.api.i18n.I18nModule,
      new play.api.mvc.CookiesModule,
      bind(classOf[TokenValidator]).toInstance(new MockedTokenValidator),
      bind(classOf[ChatRepository]).toInstance(new FakeChatRepositoryImpl()),
      bind(classOf[UserRepository]).toInstance(new FakeUserRepositoryImpl()),
      bind(classOf[EmailRepository]).toInstance(new FakeEmailRepositoryImpl()))

  lazy val injector: Injector = appBuilder.injector()
  lazy implicit val mat: Materializer = injector.instanceOf[Materializer]
  lazy val cc: ControllerComponents = injector.instanceOf[ControllerComponents]
  val actorSystem: ActorSystem = injector.instanceOf[ActorSystem]
}
