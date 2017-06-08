package com.github.dpfeiffer.errorhandling

import cats.implicits._
import cats.data.Validated
import cats.data.Validated.{invalid, valid}

trait TimeEntryValidation {

  def validateTimeEntry(t: TimeEntry): Either[TimeEntryFailure, Unit] = {
    val r = (validateBeginBeforeEnd(t) |@| validateBeginAndEndOnSameDAy(t)) map { case _ => Unit }

    r.toEither.left
      .map[TimeEntryFailure](ValidationFailed)
      .map(_ => ())
  }

  private def validateBeginBeforeEnd(t: TimeEntry): Validated[List[String], TimeEntry] = {
    if (t.begin.isBefore(t.end)) {
      valid(t)
    } else {
      invalid("begin has to be before end" :: Nil)
    }
  }

  private def validateBeginAndEndOnSameDAy(t: TimeEntry): Validated[List[String], TimeEntry] = {
    if (t.begin.toLocalDate == t.end.toLocalDate) {
      valid(t)
    } else {
      invalid("begin and end have to be on the same date" :: Nil)
    }
  }

}
