package com.github.dpfeiffer.errorhandling.transformeithert

import java.time.LocalDateTime
import java.util.UUID

import cats.data.EitherT
import cats.implicits._
import com.github.dpfeiffer.errorhandling._
import shapeless._

import scala.concurrent.{ExecutionContext, Future}

class TimeEntryService extends TimeEntryValidation {

  private[this] var timeEntries: Map[UUID, TimeEntry] = Map()

  def create(t: TimeEntry)(implicit ec: ExecutionContext): TimeEntryResult[Unit] = {
    for {
      _ <- ? <~ find(t.id).map {
        case Some(_) => Left(TimeEntryAlreadyExists(t.id))
        case None    => Right(())
      }
      _ <- ? <~ store(t)
    } yield ()
  }

  def accept(timeEntryId: UUID)(implicit ec: ExecutionContext): TimeEntryResult[Unit] = {
    for {
      t <- ? <~ find(timeEntryId).map {
        case Some(t) => Right(t)
        case None    => Left(TimeEntryDoesNotExist(timeEntryId))
      }
      _ <- ? <~ store(t.copy(status = Accepted))
    } yield ()
  }

  private def find(timeEntryId: UUID)(implicit ec: ExecutionContext): Future[Option[TimeEntry]] = Future {
    timeEntries.get(timeEntryId)
  }

  private def store(t: TimeEntry)(implicit ec: ExecutionContext): Future[Unit] = Future {
    timeEntries += (t.id -> t)
  }

}

object TransformationExample extends AsyncApp {

  override protected def run(implicit ec: ExecutionContext): Future[_] = {
    val service = new TimeEntryService()

    val entry = TimeEntry(
      id = UUID.randomUUID,
      begin = LocalDateTime.now().minusHours(1),
      end = LocalDateTime.now(),
      status = Open
    )

    //type TimeEntryResult[A] = EitherT[Future, TimeEntryFailure, A]
    val result: TimeEntryResult[Unit] = for {
      _ <- service.create(entry)
      _ <- service.create(entry)
    } yield ()

    result.fold(handleFailure, _ => println("Success!!!"))
  }

  def handleFailure(f: TimeEntryFailure): Unit = f match {
    case TimeEntryDoesNotExist(id)  => println("Sorry, the time entry does not exist.")
    case TimeEntryAlreadyExists(id) => println("Sorry, the time entry already exists.")
    case _                          =>
  }
}
