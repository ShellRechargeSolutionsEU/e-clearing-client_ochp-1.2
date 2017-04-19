package com.thenewmotion.ochp
package client

import Converters._
import api._
import org.joda.time.DateTimeZone
import org.specs2.mutable.Specification

class ChargePointSpec extends Specification {
  "converting a ChargePoint into a ChargePointInfo and back returns the original value" >> {
    val cp = ChargePoint(
      evseId = EvseId("DE*823*E1234*5678"),
      locationId = "WERELD",
      locationName = "Keizersgracht-585",
      locationNameLang = "NLD",
      relatedResources = List(
        RelatedResource("https://my.thenewmotion.com", RelatedResourceTypeEnum.operatorMap)),
      address = CpAddress(
        address = "Keizersgracht 585",
        city = "Amsterdam",
        zipCode = "1017DR",
        country = "NLD"),
      chargePointLocation = GeoPoint(52.364208, 4.891792),
      relatedLocations = List(
        AdditionalGeoPoint(GeoPoint(40, 12), None, GeoPointTypeEnum.ui),
        AdditionalGeoPoint(GeoPoint(41, 13), Some("Keizersgracht 583"), GeoPointTypeEnum.access),
        AdditionalGeoPoint(GeoPoint(41, 13.000001), Some("Keizersgracht 585"), GeoPointTypeEnum.access),
        AdditionalGeoPoint(GeoPoint(42, 14), None, GeoPointTypeEnum.exit),
        AdditionalGeoPoint(GeoPoint(42, 14.000001), None, GeoPointTypeEnum.exit),
        AdditionalGeoPoint(GeoPoint(43, 15), None, GeoPointTypeEnum.other)),
      timeZone = Option(DateTimeZone.forID("Europe/Amsterdam")),
      location = GeneralLocation.`on-street`,
      authMethods = List(AuthMethod.RfidMifareCls),
      connectors = List(Connector(ConnectorStandard.`TESLA-R`,ConnectorFormat.Socket)),
      ratings = Some(Ratings(22f, None, Some(220))),
      category = Some("5"),
      operatingTimes = Some(Hours(
        regularHoursOrTwentyFourSeven = Left(List(RegularHours(
          weekday = 1,
          periodBegin = TimeNoSecs("08:00"),
          periodEnd = TimeNoSecs("18:00")))),
        exceptionalOpenings = List(),
        exceptionalClosings = List())))

    cpInfoToChargePoint(chargePointToCpInfo(cp)) must beSome(cp)
  }
}