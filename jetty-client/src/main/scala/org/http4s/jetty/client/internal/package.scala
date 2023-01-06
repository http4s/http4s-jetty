package org.http4s.jetty.client

import cats.effect.kernel.Sync
import org.log4s.Logger

package object internal {
  // TODO This should go away in 0.24.
  private[http4s] def loggingAsyncCallback[F[_], A](
      logger: Logger
  )(attempt: Either[Throwable, A])(implicit F: Sync[F]): F[Unit] =
    attempt match {
      case Left(e) => F.delay(logger.error(e)("Error in asynchronous callback"))
      case Right(_) => F.unit
    }
}
