package com.thenewmotion.ochp
package client

import Converters._
import api._
import org.specs2.mutable.Specification

class RatingsTypeSpec extends Specification {
  "an empty ratings type is empty" >> {
    toRatingsType(toRatingsOption(null)) mustEqual null
  }

  "guaranteed power may be omitted" >> {
    val expected = Some(Ratings(22f, None, Some(220)))

    toRatingsOption(toRatingsType(expected)) mustEqual expected
  }

  "nominal voltage may be omitted" >> {
    val expected = Some(Ratings(22f, Some(22f), None))

    toRatingsOption(toRatingsType(expected)) mustEqual expected
  }
}