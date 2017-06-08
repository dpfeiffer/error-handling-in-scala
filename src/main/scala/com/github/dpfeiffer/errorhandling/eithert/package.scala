package com.github.dpfeiffer.errorhandling

import cats.data.EitherT

import scala.concurrent.Future

package object eithert {
  type TimeEntryResult[A] = EitherT[Future, TimeEntryFailure, A]
}
