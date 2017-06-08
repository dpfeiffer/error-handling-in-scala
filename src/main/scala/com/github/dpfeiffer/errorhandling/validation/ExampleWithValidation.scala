package com.github.dpfeiffer.errorhandling.validation

import java.time.LocalDateTime
import java.util.UUID

import cats.implicits._
import com.github.dpfeiffer.errorhandling._

class TimeEntryService extends TimeEntryValidation {

  private[this] var timeEntries: Map[UUID, TimeEntry] = Map()

  def create(t: TimeEntry): TimeEntryResult[Unit] = {
    for {
      _ <- validateTimeEntry(t)
      _ <- store(t)
    } yield ()
  }

  private def store(t: TimeEntry): TimeEntryResult[Unit] = {
    timeEntries += (t.id -> t)
    ().asRight
  }

}

object ExampleWithValidation extends App {

  val service = new TimeEntryService()

  val entry = TimeEntry(
    id = UUID.randomUUID(),
    begin = LocalDateTime.now(),
    end = LocalDateTime.now().minusDays(1),
    status = Open
  )

  val result = service.create(entry)

  result.fold(handleFailure, _ => println("Success!!!"))

  def handleFailure(f: TimeEntryFailure): Unit = f match {
    case TimeEntryDoesNotExist(id)  => println("Sorry, the time entry does not exist.")
    case TimeEntryAlreadyExists(id) => println("Sorry, the time entry already exists.")
    case ValidationFailed(failures) => println(s"""Sorry, the validation failed with following errors:
         |${failures.mkString("\n")}""".stripMargin)
  }

}
