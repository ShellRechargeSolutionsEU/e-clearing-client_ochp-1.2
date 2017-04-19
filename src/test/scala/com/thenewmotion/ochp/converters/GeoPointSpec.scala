package com.thenewmotion.ochp
package converters

import api._
import GeoPointConverters._

class GeoPointSpec extends Spec {
  "coordinates must be provided" >> {
    GeoPoint("", "") must throwA[IllegalArgumentException]
  }

  "several number formats are supported for coordinates" >> {
    fmt(-123.1234567) === "-123.123457"
    fmt(123.1234567)  === "123.123457"
    fmt(-0.0004567) === "-0.000457"
    fmt(.0004567) === "0.000457"
  }

  "converting geo points to ochp and back yields the original value" >> {
    import GeoPointConverter._

    val value = GeoPoint(41, 12)

    fromOchp(toOchp(value)) mustEqual value
  }

  "converting additional geo points to ochp and back yields the original value" >> {
    import AdditionalGeoPointConverter._

    val value = AdditionalGeoPoint(
      GeoPoint(41, 12),
      None,
      GeoPointTypes.entrance
    )

    fromOchp(toOchp(value)) mustEqual value
  }
}