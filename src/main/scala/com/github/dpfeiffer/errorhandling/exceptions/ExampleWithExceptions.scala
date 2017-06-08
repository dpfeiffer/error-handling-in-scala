package com.github.dpfeiffer.errorhandling.exceptions

import java.time._
import java.util.UUID

import com.github.dpfeiffer.errorhandling._

class TimeEntryService {

  private[this] var timeEntries: Map[UUID, TimeEntry] = Map()

  def create(t: TimeEntry): Unit = {
    if (timeEntries.contains(t.id)) {
      throw TimeEntryAlreadyExistsException(t.id)
    } else {
      timeEntries = timeEntries + (t.id -> t)
    }
  }

  def accept(id: UUID): Unit = {
    timeEntries.get(id) match {
      case Some(t) =>
        val updated = t.copy(status = Accepted)
        timeEntries = timeEntries + (id -> updated)
      case None =>
        throw TimeEntryDoesNotExistException(id)
    }
  }
}

object ExampleWithExceptions extends App {

  val service = new TimeEntryService

  val entry = TimeEntry(
    id = UUID.randomUUID,
    begin = LocalDateTime.now.minusHours(1),
    end = LocalDateTime.now,
    status = Open
  )

  try {
    service.create(entry)
    service.accept(entry.id)
  } catch {
    case TimeEntryDoesNotExistException(id)  => println("Sorry the time entry does not exist.")
    case TimeEntryAlreadyExistsException(id) => println("Sorry the time entry does already exist.")
  }

}
