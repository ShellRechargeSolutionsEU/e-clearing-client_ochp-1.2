package com.thenewmotion.ochp
package client

import Converters._
import api._
import eu.ochp._1.HoursType
import org.specs2.mutable.Specification

class HourlyTypeSpec extends Specification {

  "an undefined instance is equivalent to None" >> {
    val toScala = toHoursOption(null)

    toScala must beSuccessfulTry(None)
    hoursOptionToHoursType(toScala.toOption.flatten) mustEqual null
  }

  "when no regular/exceptional hours are provided, 24/7 must be defined and true" >> {
    val undefined = new HoursType {
      setTwentyfourseven(null)
    }
    val notTwentyFourSeven = new HoursType {
      setTwentyfourseven(false)
    }
    val twentyFourSeven = new HoursType {
      setTwentyfourseven(true)
    }

    regularOpeningsAreDefined(undefined) must beFailedTry
    regularOpeningsAreDefined(notTwentyFourSeven) must beFailedTry
    regularOpeningsAreDefined(twentyFourSeven) must beSuccessfulTry
  }

  "24/7 can be defined in alternative to regular hours, possibly with exceptions" >> {
    val hours = Some(
      Hours(
        regularHoursOrTwentyFourSeven = Right(true),
        Nil,
        exceptionalClosings = List(
          ExceptionalPeriod(
            DateTimeNoMillis("2015-01-01T00:00:00+00:00"),
            DateTimeNoMillis("2015-01-02T00:00:00+00:00")))))

    toHoursOption(hoursOptionToHoursType(hours)) must beSuccessfulTry(hours)
  }

  "regular hours can be defined in alternative to 24/7" >> {
    val hours = Some(
      Hours(
        regularHoursOrTwentyFourSeven = Left(List(RegularHours(
          weekday = 1,
          periodBegin = TimeNoSecs("08:00"),
          periodEnd = TimeNoSecs("18:00")))),
        Nil,
        Nil))

    toHoursOption(hoursOptionToHoursType(hours)) must beSuccessfulTry(hours)
  }
}