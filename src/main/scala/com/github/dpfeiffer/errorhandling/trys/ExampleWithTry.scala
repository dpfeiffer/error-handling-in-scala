package com.github.dpfeiffer.errorhandling.trys

import java.time.LocalDateTime
import java.util.UUID

import scala.util._

import com.github.dpfeiffer.errorhandling._

class TimeEntryService {

  private[this] var timeentries: Map[UUID, TimeEntry] = Map()

  def create(timeEntry: TimeEntry): Try[Unit] = {
    if (timeentries.contains(timeEntry.id)) {
      Failure(TimeEntryAlreadyExistsException(timeEntry.id))
    } else {
      timeentries += (timeEntry.id -> timeEntry)
      Success(())
    }
  }

  def accept(timeEntryId: UUID): Try[Unit] = {
    for {
      t <- get(timeEntryId)
      _ <- store(t.copy(status = Accepted))
    } yield ()
  }

  private[this] def get(timeEntryId: UUID): Try[TimeEntry] = {
    timeentries.get(timeEntryId) match {
      case Some(t) => Success(t)
      case None    => Failure(TimeEntryDoesNotExistException(timeEntryId))
    }
  }

  private[this] def store(timeEntry: TimeEntry): Try[Unit] = Try {
    timeentries += (timeEntry.id -> timeEntry)
    ()
  }

}

object ExampleWithTry extends App {

  val service = new TimeEntryService

  val timeEntry = TimeEntry(
    id = UUID.randomUUID,
    begin = LocalDateTime.now.minusHours(1),
    end = LocalDateTime.now,
    status = Open
  )

  val result: Try[Unit] = for {
    _ <- service.create(timeEntry)
    _ <- service.accept(timeEntry.id)
    _ <- service.create(timeEntry)
  } yield ()

  result match {
    case Success(_)  => println("Succeeded.")
    case Failure(ex) => println(s"Exception occurred. ${ex.getMessage}")
  }

}
