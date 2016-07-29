package com.thenewmotion.ochp
package converters

import api.{ChargePointSchedule, ChargePointStatus}
import eu.ochp.{_1 => j}
import DateTimeConverters._

object ChargePointScheduleConverter {
  def toOchp(schedule: ChargePointSchedule): j.ChargePointScheduleType = {
    new j.ChargePointScheduleType {
      setStatus(new j.ChargePointStatusType {
        setChargePointStatusType(schedule.status.toString)
      })
      setStartDate(Utc.toOchp(schedule.startDate))
      schedule.endDate.map(Utc.toOchp).foreach(setEndDate)
    }
  }

  def fromOchp(schedule: j.ChargePointScheduleType) = {
    val begin = Utc.fromOchp(schedule.getStartDate)
    val end = Option(schedule.getEndDate).map(Utc.fromOchp)
    val status = ChargePointStatus.withName(schedule.getStatus.getChargePointStatusType)

    ChargePointSchedule(begin, end, status)
  }
}