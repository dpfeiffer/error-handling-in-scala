package com.github.dpfeiffer.errorhandling.either

import java.util.UUID

import com.github.dpfeiffer.errorhandling._
import java.time.LocalDateTime

class TimeEntryService {

  private[this] var timeEntries: Map[UUID, TimeEntry] = Map()

  def create(t: TimeEntry): TimeEntryResult[Unit] = {
    if (timeEntries.contains(t.id)) {
      Left(TimeEntryAlreadyExists(t.id))
    } else {
      timeEntries = timeEntries + (t.id -> t)
      Right(())
    }
  }

  def accept(timeEntryId: UUID): TimeEntryResult[Unit] = {
    for {
      t <- get(timeEntryId)
      _ <- store(t.copy(status = Accepted))
    } yield ()
  }

  private def get(timeEntryId: UUID): TimeEntryResult[TimeEntry] = {
    timeEntries.get(timeEntryId) match {
      case Some(t) => Right(t)
      case None    => Left(TimeEntryDoesNotExist(timeEntryId))
    }
  }

  private def store(t: TimeEntry): TimeEntryResult[Unit] = {
    timeEntries += (t.id -> t)
    Right(())
  }
}

object ExampleWithEither extends App {

  val service = new TimeEntryService()

  val entry = TimeEntry(
    id = UUID.randomUUID,
    begin = LocalDateTime.now().minusHours(1),
    end = LocalDateTime.now(),
    status = Open
  )

  //type TimeEntryResult[A] = Either[TimeEntryFailure,A]
  val result: TimeEntryResult[Unit] = for {
    _ <- service.create(entry)
    _ <- service.create(entry)
    _ <- service.accept(entry.id)
  } yield ()

  result.fold(
    {
      case TimeEntryDoesNotExist(id)  => println("Sorry, the time entry does not exist.")
      case TimeEntryAlreadyExists(id) => println("Sorry, the time entry already exists.")
      case _ =>
    }, { _ =>
      println("Yeaaah, success!")
    }
  )

  def handleFailure(f: TimeEntryFailure): Unit = {
    f match {
      case TimeEntryDoesNotExist(id)  => println("Sorry, the time entry does not exist.")
      case TimeEntryAlreadyExists(id) => println("Sorry, the time entry already exists.")
      case _                          =>
    }
  }

}
