package com.github.dpfeiffer.errorhandling

import java.time._
import java.util.UUID

sealed trait TimeEntryStatus
case object Open     extends TimeEntryStatus
case object Accepted extends TimeEntryStatus
case object Declined extends TimeEntryStatus

case class TimeEntry(
    id: UUID,
    begin: LocalDateTime,
    end: LocalDateTime,
    status: TimeEntryStatus
)
