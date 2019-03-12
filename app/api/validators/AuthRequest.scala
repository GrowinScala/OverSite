package api.validators

import play.api.mvc.{ Request, WrappedRequest }

import scala.concurrent.Future

/** Case class created to replace the first parameter of ActionBuilder */
case class AuthRequest[A](
  userName: Future[String],
  request: Request[A]) extends WrappedRequest[A](request) {
  override def newWrapper[B](newRequest: Request[B]): AuthRequest[B] =
    AuthRequest(
      userName,
      super.newWrapper(newRequest))
}
