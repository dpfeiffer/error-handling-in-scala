package com.github.dpfeiffer.errorhandling

import scala.concurrent.Future
import scala.util._

package object futureeither {
  type TimeEntryResult[A]    = Future[Either[TimeEntryFailure, A]]
  type TimeEntryFailureOr[A] = Either[TimeEntryFailure, A]
}
