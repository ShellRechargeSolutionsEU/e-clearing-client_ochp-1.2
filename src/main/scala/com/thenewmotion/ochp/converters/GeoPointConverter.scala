package com.thenewmotion.ochp
package converters

import api.{AdditionalGeoPoint, GeoPoint, GeoPointTypes}
import eu.ochp._1.{AdditionalGeoPointType, GeoPointType}

object GeoPointConverters {
  def fmt(x: Double) = "%3.6f".formatLocal(java.util.Locale.US, x)

  object GeoPointConverter {
    def fromOchp(value: GeoPointType) =
      GeoPoint(value.getLat, value.getLon)

    def toOchp(point: GeoPoint): GeoPointType = {
      new GeoPointType {
        setLat(fmt(point.lat))
        setLon(fmt(point.lon))
      }
    }
  }

  object AdditionalGeoPointConverter {
    def fromOchp(value: AdditionalGeoPointType) =
      AdditionalGeoPoint(
        GeoPoint(value.getLat, value.getLon),
        Option(value.getName),
        GeoPointTypes.withName(value.getType))

    def toOchp(value: AdditionalGeoPoint) =
      new AdditionalGeoPointType {
        setLat(fmt(value.point.lat))
        setLon(fmt(value.point.lon))
        value.name.foreach(setName)
        setType(value.typ.toString)
      }
  }
}