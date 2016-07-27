package com.thenewmotion.ochp
package client

import Converters._
import api._
import eu.ochp._1.{ConnectorFormat => GenConnectorFormat, ConnectorStandard => GenConnectorStandard, _}
import org.joda.time.DateTimeZone
import org.specs2.mutable.Specification

class ChargePointSpec extends Specification {
  "converting a ChargePoint into a ChargePointInfo and back returns the original value" >> {
    val cp = ChargePoint(
      evseId = EvseId("DE*823*E1234*5678"),
      locationId = "WERELD",
      locationName = "Keizersgracht-585",
      locationNameLang = "NLD",
      address = CpAddress(
        address = "Keizersgracht 585",
        city = "Amsterdam",
        zipCode = "1017DR",
        country = "NLD"),
      geoLocation = GeoPoint(52.364208, 4.891792),
      geoUserInterface = Some(GeoPoint(40, 12)),
      geoSiteAccess = List(GeoPoint(42, 13)),
      geoSiteEntrance = List(GeoPoint(43, 14)),
      geoSiteExit = List(GeoPoint(44, 15)),
      geoSiteOther = List(GeoPoint(45, 16)),
      timeZone = Option(DateTimeZone.forID("Europe/Amsterdam")),
      location = GeneralLocation.`on-street`,
      authMethods = List(AuthMethod.RfidMifareCls),
      connectors = List(Connector(ConnectorStandard.`TESLA-R`,ConnectorFormat.Socket)),
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