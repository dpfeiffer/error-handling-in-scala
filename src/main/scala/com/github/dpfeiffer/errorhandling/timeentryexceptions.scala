package com.github.dpfeiffer.errorhandling

import java.util.UUID

abstract class TimeEntryException(message: String) extends Exception
case class TimeEntryAlreadyExistsException(id: UUID)
    extends TimeEntryException(s"Time Entry with id $id already exists.")
case class TimeEntryDoesNotExistException(id: UUID)
    extends TimeEntryException(s"Time Entry with id $id does not exist.")
