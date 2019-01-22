package api.controllers

import akka.actor.ActorSystem
import api.validators.TokenValidator
import database.repository.{ChatRepositoryImpl, UserRepositoryImpl}
import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import slick.jdbc.MySQLProfile.api._


import scala.concurrent.ExecutionContext

class DraftsController @Inject() (
 tokenValidator: TokenValidator,
 cc: ControllerComponents,
 actorSystem: ActorSystem,
 implicit val db: Database,
 chatActions: ChatRepositoryImpl,
 usersActions: UserRepositoryImpl)(implicit exec: ExecutionContext) extends AbstractController(cc) {



}