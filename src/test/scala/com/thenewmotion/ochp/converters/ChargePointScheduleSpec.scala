package com.thenewmotion.ochp
package converters

import api.{ChargePointSchedule, ChargePointStatus, DateTimeNoMillis}
import converters.ChargePointScheduleConverter._


class ChargePointScheduleSpec extends Spec {
  "converting to Ochp and back yields the original value" >> {
    val schedule = ChargePointSchedule(
      startDate = DateTimeNoMillis("2016-01-01T00:00:00+01:00"),
      endDate = None,
      status = ChargePointStatus.Operative)

    fromOchp(toOchp(schedule)) mustEqual schedule
  }
}