package api.controllers.unitTest

import akka.actor.ActorSystem
import akka.stream.Materializer
import api.validators.{ MockedInvalidTokenValidator, MockedValidTokenValidator, TokenValidator }
import database.repository.fake.{ FakeChatRepositoryImpl, FakeEmailRepositoryImpl, FakeUserRepositoryImpl }
import database.repository.{ ChatRepository, EmailRepository, UserRepository }
import javax.inject.Singleton
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ Injector, bind }
import play.api.mvc.ControllerComponents

@Singleton
object UnitControllerTestsAppBuilder {
  val appBuilderWithValidToken: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
    .load(
      new play.api.inject.BuiltinModule,
      new play.api.i18n.I18nModule,
      new play.api.mvc.CookiesModule,
      bind(classOf[TokenValidator]).toInstance(new MockedValidTokenValidator),
      bind(classOf[ChatRepository]).toInstance(new FakeChatRepositoryImpl()),
      bind(classOf[UserRepository]).toInstance(new FakeUserRepositoryImpl()),
      bind(classOf[EmailRepository]).toInstance(new FakeEmailRepositoryImpl()))

  val appBuilderWithInvalidToken: GuiceApplicationBuilder = new GuiceApplicationBuilder().in(Mode.Test)
    .load(
      new play.api.inject.BuiltinModule,
      new play.api.i18n.I18nModule,
      new play.api.mvc.CookiesModule,
      bind(classOf[TokenValidator]).toInstance(new MockedInvalidTokenValidator),
      bind(classOf[ChatRepository]).toInstance(new FakeChatRepositoryImpl()),
      bind(classOf[UserRepository]).toInstance(new FakeUserRepositoryImpl()),
      bind(classOf[EmailRepository]).toInstance(new FakeEmailRepositoryImpl()))

  lazy val injectorWithValidToken: Injector = appBuilderWithValidToken.injector()

  lazy val matWithValidToken: Materializer = injectorWithValidToken.instanceOf[Materializer]
  lazy val ccWithValidToken: ControllerComponents = injectorWithValidToken.instanceOf[ControllerComponents]
  val actorSystemWithValidToken: ActorSystem = injectorWithValidToken.instanceOf[ActorSystem]

  lazy val injectorWithInvalidToken: Injector = appBuilderWithInvalidToken.injector()

  lazy val matWithInvalidToken: Materializer = injectorWithInvalidToken.instanceOf[Materializer]
  lazy val ccWithInvalidToken: ControllerComponents = injectorWithInvalidToken.instanceOf[ControllerComponents]
  val actorSystemWithInvalidToken: ActorSystem = injectorWithInvalidToken.instanceOf[ActorSystem]

}
