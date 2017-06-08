package com.github.dpfeiffer.errorhandling

import java.util.UUID

sealed trait TimeEntryFailure

case class ValidationFailed(validationFailures: List[String]) extends TimeEntryFailure
case class TimeEntryDoesNotExist(id: UUID)                     extends TimeEntryFailure
case class TimeEntryAlreadyExists(id: UUID)                    extends TimeEntryFailure
