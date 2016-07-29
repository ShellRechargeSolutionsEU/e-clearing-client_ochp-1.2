package com.thenewmotion.ochp
package converters

import api.{ChargePointSchedule, ChargePointStatus, DateTimeNoMillis}
import converters.ChargePointScheduleConverter._
import org.specs2.mutable.Specification


class ChargePointScheduleSpec extends Specification {
  "converting to Ochp and back yields the original value" >> {
    val schedule = ChargePointSchedule(
      startDate = DateTimeNoMillis("2016-01-01T00:00:00+01:00"),
      endDate = None,
      status = ChargePointStatus.Operative)

    fromOchp(toOchp(schedule)) mustEqual schedule
  }
}