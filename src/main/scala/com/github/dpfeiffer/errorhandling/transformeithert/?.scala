package com.github.dpfeiffer.errorhandling.transformeithert

import cats.implicits._
import cats.data.EitherT
import com.github.dpfeiffer.errorhandling.TimeEntryFailure
import shapeless._

import scala.concurrent.{ExecutionContext, Future}

object ? {
  def <~[A](x: A)(implicit ec: ExecutionContext): TimeEntryResult[A] = {
    x.pure[TimeEntryResult]
  }

  def <~[A](x: Either[TimeEntryFailure, A])(implicit ec: ExecutionContext): TimeEntryResult[A] = {
    EitherT.fromEither[Future](x)
  }

  def <~[A](x: Future[A])(implicit ev: A <:!< Either[TimeEntryFailure, _], ec: ExecutionContext): TimeEntryResult[A] = {
    EitherT.right[Future, TimeEntryFailure, A](x)
  }

  def <~[A](x: Future[Either[TimeEntryFailure, A]]): TimeEntryResult[A] = {
    EitherT(x)
  }
}
