package com.thenewmotion.ochp
package client

import Converters._
import api._
import eu.ochp._1.HoursType
import org.specs2.mutable.Specification

class HourlyTypeSpec extends Specification {

  "an undefined instance is equivalent to None" >> {
    hoursOptionToHoursType(toHoursOption(null)) mustEqual null
  }

  "an undefined instance is not equivalent to one that does not define any value" >> {
    toHoursOption(null) mustEqual None
    toHoursOption(new HoursType) must throwA[IllegalArgumentException]
  }

  "when no regular/exceptional hours are provided, 24/7 must be defined and true" >> {
    val undefined = {
      val ht = new HoursType
      ht.setTwentyfourseven(null)
      ht
    }
    val notTwentyFourSeven = {
      val ht = new HoursType
      ht.setTwentyfourseven(false)
      ht
    }
    val twentyFourSeven = {
      val ht = new HoursType
      ht.setTwentyfourseven(true)
      ht
    }

    validate(undefined) must throwA[IllegalArgumentException]
    validate(notTwentyFourSeven) must throwA[IllegalArgumentException]
    validate(twentyFourSeven) must beTrue
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

    toHoursOption(hoursOptionToHoursType(hours)) mustEqual hours
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

    toHoursOption(hoursOptionToHoursType(hours)) mustEqual hours
  }
}