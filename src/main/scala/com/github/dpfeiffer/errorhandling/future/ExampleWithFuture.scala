package com.github.dpfeiffer.errorhandling.future

import java.time.LocalDateTime
import java.util.UUID

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util._

import com.github.dpfeiffer.errorhandling._

class TimeEntryService {

  private[this] var timeentries: Map[UUID, TimeEntry] = Map()

  def create(timeEntry: TimeEntry)(implicit ec: ExecutionContext): Future[Unit] = {
    for{
      exists <- exists(timeEntry.id)
      _ <- if (exists) {
        Future.failed(TimeEntryAlreadyExistsException(timeEntry.id))
      } else {
        store(timeEntry)
      }
    } yield ()
  }

  def accept(timeEntryId: UUID)(implicit ec: ExecutionContext): Future[Unit] = {
    for {
      t <- get(timeEntryId)
      _ <- store(t.copy(status = Accepted))
    } yield ()
  }

  private[this] def get(timeEntryId: UUID)(implicit ec: ExecutionContext): Future[TimeEntry] = Future {
    timeentries.get(timeEntryId) match {
      case Some(t) => t
      case None    => throw TimeEntryDoesNotExistException(timeEntryId)
    }
  }

  private[this] def exists(id: UUID)(implicit ec: ExecutionContext): Future[Boolean] = Future{
    timeentries.contains(id)
  }

  private[this] def store(timeEntry: TimeEntry)(implicit ec: ExecutionContext): Future[Unit] = Future {
    timeentries += (timeEntry.id -> timeEntry)
    ()
  }

}

object ExampleWithFuture extends AsyncApp {

  override protected def run(implicit ec: ExecutionContext): Future[_] = {
    val service = new TimeEntryService

    val timeEntry = TimeEntry(
      id = UUID.randomUUID,
      begin = LocalDateTime.now.minusHours(1),
      end = LocalDateTime.now,
      status = Open
    )

    val result = for {
      _ <- service.create(timeEntry)
      _ <- service.accept(timeEntry.id)
      _ <- service.create(timeEntry)
    } yield ()

    result.onComplete {
      case Success(_)  => println("Succeeded.")
      case Failure(ex) => println(s"Exception occurred. ${ex.getMessage}")
    }

    result
  }
}
