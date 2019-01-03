package actions

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration

class SupportActions {
  protected def waitToComplete[T](x: Future[T]): T = {
    Await.result(x, Duration.Inf)
  }
}
