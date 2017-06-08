package com.github.dpfeiffer.errorhandling.futureeither

import com.github.dpfeiffer.errorhandling._

import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.UUID

import scala.util._
import scala.concurrent.{ExecutionContext, Future}

class TimeEntryService {

  private[this] var timeEntries: Map[UUID, TimeEntry] = Map()

  def create(t: TimeEntry)(implicit ec: ExecutionContext): TimeEntryResult[Unit] = Future {
    if (timeEntries.contains(t.id)) {
      Left(TimeEntryAlreadyExists(t.id))
    } else {
      timeEntries = timeEntries + (t.id -> t)
      Right(())
    }
  }

  def accept(timeEntryId: UUID)(implicit ec: ExecutionContext): TimeEntryResult[Unit] = {
    for {
      f1 <- get(timeEntryId)
      f2 <- f1 match {
        case Left(f)  => Future.successful(Left(f))
        case Right(t) => store(t.copy(status = Accepted))
      }
    } yield f2
  }

  private def get(timeEntryId: UUID)(implicit ec: ExecutionContext): TimeEntryResult[TimeEntry] = Future {
    timeEntries.get(timeEntryId) match {
      case Some(t) => Right(t)
      case None    => Left(TimeEntryDoesNotExist(timeEntryId))
    }
  }

  private def store(t: TimeEntry)(implicit ec: ExecutionContext): TimeEntryResult[Unit] = Future {
    timeEntries += (t.id -> t)
    Right(())
  }
}

object ExampleWithFutureEither extends AsyncApp {

  override protected def run(implicit ec: ExecutionContext): Future[_] = {
    val service = new TimeEntryService()

    val entry = TimeEntry(
      id = UUID.randomUUID,
      begin = LocalDateTime.now().minusHours(1),
      end = LocalDateTime.now(),
      status = Open
    )

    //type TimeEntryResult[A] = Future[Either[TimeEntryFailure, A]]
    val result: TimeEntryResult[Unit] = for {
      r1 <- service.create(entry)
      r2 <- r1 match {
        case Left(f)  => Future.successful[TimeEntryFailureOr[Unit]](Left(f))
        case Right(_) => service.create(entry)
      }
      r3 <- r2 match {
        case Left(f)  => Future.successful[TimeEntryFailureOr[Unit]](Left(f))
        case Right(_) => service.accept(entry.id)
      }
    } yield r3

    result.onComplete {
      case Failure(t) => println(s"An exception occurred. $t")
      case Success(e) => handleResult(e)
    }

    result
  }

  def handleResult(e: Either[TimeEntryFailure, Unit]): Unit = e match {
    case Left(TimeEntryDoesNotExist(id))  => println("Sorry, the time entry does not exist.")
    case Left(TimeEntryAlreadyExists(id)) => println("Sorry, the time entry already exists.")
    case Left(_)                          =>
    case Right(_)                         => println("Yeaaah, success!")
  }
}
