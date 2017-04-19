package com.thenewmotion.ochp
package converters

import api.DateTimeNoMillis
import com.thenewmotion.time.Imports._
import eu.ochp._1.{DateTimeType, LocalDateTimeType}


trait DateTimeConverters {
  object WithOffset {
    def fromOchp(dateTime: LocalDateTimeType) =
      DateTimeNoMillis(dateTime.getLocalDateTime)

    def toOchp(dateTime: DateTime): LocalDateTimeType = {
      new LocalDateTimeType {
        setLocalDateTime(DateTimeNoMillis(dateTime).toString)
      }
    }
  }

  object Utc {
    def toOchp(date: DateTime): DateTimeType = {
      new DateTimeType {
        setDateTime(DateTimeNoMillis(date.withZone(DateTimeZone.UTC)).toString)
      }
    }

    def fromOchp(value: DateTimeType): DateTime =
      DateTimeNoMillis(value.getDateTime)
  }
}

object DateTimeConverters extends DateTimeConverters