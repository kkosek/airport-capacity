package airportcapacity.utils

import java.time.Duration
import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration

object DurationExtensions {
  implicit class DurationOps(d: Duration) {
    def toFiniteDuration: FiniteDuration = FiniteDuration.apply(d.toNanos, TimeUnit.NANOSECONDS)
  }
}
