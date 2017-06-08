package com.github.dpfeiffer.errorhandling

package object either{
  type TimeEntryResult[A] = Either[TimeEntryFailure, A]
}
