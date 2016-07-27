package com.thenewmotion.ochp
package client

import Converters._
import api._
import org.specs2.mutable.Specification

class GeoPointSpec extends Specification {
  "coordinates must be provided" >> {
    GeoPoint("", "") must throwA[IllegalArgumentException]
  }

  "several number formats are supported for coordinates" >> {
    GeoPoint.fmt(-123.1234567) === "-123.123457"
    GeoPoint.fmt(123.1234567)  === "123.123457"
    GeoPoint.fmt(-0.0004567) === "-0.000457"
    GeoPoint.fmt(.0004567) === "0.000457"
  }
}