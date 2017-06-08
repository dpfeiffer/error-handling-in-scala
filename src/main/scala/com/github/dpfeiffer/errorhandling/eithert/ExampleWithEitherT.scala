package com.github.dpfeiffer.errorhandling.eithert

import java.time.LocalDateTime
import java.util.UUID

import cats.data.EitherT
import cats.implicits._
import com.github.dpfeiffer.errorhandling._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class TimeEntryService {

  private[this] var timeEntries: Map[UUID, TimeEntry] = Map()

  def create(t: TimeEntry)(implicit ec: ExecutionContext): TimeEntryResult[Unit] = {
    for {
      _ <- EitherT(
        find(t.id).map {
          case Some(_) => Left(TimeEntryAlreadyExists(t.id))
          case None    => Right(())
        }
      )
      _ <- EitherT.right(store(t))
    } yield ()
  }

  def accept(timeEntryId: UUID)(implicit ec: ExecutionContext): TimeEntryResult[Unit] = {
    for {
      optionalTE <- EitherT.right(find(timeEntryId))
      t <- optionalTE match {
        case Some(value) => EitherT.right(Future.successful(value))
        case None        => EitherT.left(Future.successful(TimeEntryDoesNotExist(timeEntryId)))
      }
      _ <- EitherT.right(store(t.copy(status = Accepted)))
    } yield ()
  }

  private def find(timeEntryId: UUID)(implicit ec: ExecutionContext): Future[Option[TimeEntry]] = Future {
    timeEntries.get(timeEntryId)
  }

  private def store(t: TimeEntry)(implicit ec: ExecutionContext): Future[Unit] = Future {
    timeEntries += (t.id -> t)
    ().asRight
  }

}

object ExampleWithEitherT extends AsyncApp {

  override protected def run(implicit ec: ExecutionContext): Future[_] = {
    val service = new TimeEntryService()

    val now   = LocalDateTime.now()
    val entry = TimeEntry(UUID.randomUUID, now.minusHours(1), now, Open)

    //type TimeEntryResult[A] = EitherT[Future,TimeEntryFailure,A]
    val result: TimeEntryResult[Unit] = for {
      _ <- service.create(entry)
      _ <- service.create(entry)
      _ <- service.accept(entry.id)
    } yield ()

    val futureResult: Future[Unit] = result.fold(handleFailure, _ => println("Success!!!"))

    futureResult.onComplete {
        case Failure(t) => println("Oh noooo!")
        case Success(_) => println("Oh noooo!")
    }

    futureResult
  }

  def handleFailure(f: TimeEntryFailure): Unit = f match {
    case TimeEntryDoesNotExist(id)  => println("Sorry, the time entry does not exist.")
    case TimeEntryAlreadyExists(id) => println("Sorry, the time entry already exists.")
    case _                          =>
  }
}
