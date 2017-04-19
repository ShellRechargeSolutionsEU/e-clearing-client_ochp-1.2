package com.thenewmotion.ochp
package converters

import api.Ratings
import eu.ochp.{_1 => j}

trait RatingsConverter {
  def toOchp(value: Ratings): j.RatingsType = {
    val Ratings(max, guaranteed, voltage) = value
    new j.RatingsType {
      setMaximumPower(max)
      guaranteed.foreach(setGuaranteedPower(_))
      voltage.foreach(setNominalVoltage(_))
    }
  }

  def fromOchp(ratings: j.RatingsType): Ratings =
    Ratings(
      ratings.getMaximumPower,
      Option(ratings.getGuaranteedPower).map(_.toFloat),
      Option(ratings.getNominalVoltage).map(_.toInt))
}

object RatingsConverter extends RatingsConverter