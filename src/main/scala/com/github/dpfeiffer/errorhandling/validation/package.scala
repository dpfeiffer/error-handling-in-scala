package com.github.dpfeiffer.errorhandling

package object validation{
  type TimeEntryResult[A] = Either[TimeEntryFailure, A]
}
