package com.thenewmotion.ochp
package client

import Converters._
import api._
import eu.ochp._1.{CdrStatusType => GenCdrStatusType, ConnectorFormat => GenConnectorFormat, ConnectorStandard => GenConnectorStandard, ConnectorType => GenConnectorType, EmtId => GenEmtId, _}
import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ConverterSpec extends Specification {
  "converting a Token into RoamingAuthorisationInfo and back returns the original value" >> {
    val token1 = ChargeToken(
      contractId = "YYABCC00000003",
      emtId = EmtId(
        tokenSubType = Some(TokenSubType.withName("mifareCls")),
        tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0"),
      printedNumber = Some("YYABCC00000003J"),
      expiryDate = DateTimeNoMillis("2014-07-14T02:00:00+02:00"))

    roamingAuthorisationInfoToToken(tokenToRoamingAuthorisationInfo(token1)) mustEqual token1
  }

  "converting a CDR into CDRInfo and back returns the original value" >> {
    val cdr = CDR(
      cdrId = "123456someId123456",
      evseId = "FR*A23*E45B*78C",
      emtId = EmtId(
        tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0",
        tokenType = TokenType.withName("rfid"),
        tokenSubType = Some(TokenSubType.withName("mifareCls"))),
      contractId = "DE-LND-C00001516-E",
      liveAuthId = Some("wtf"),
      status = CdrStatus.withName("new"),
      startDateTime = DateTimeNoMillis("2014-08-08T10:10:10+01:00"),
      endDateTime = DateTimeNoMillis("2014-08-08T18:10:10+01:00"),
      duration = Some("200"),
      houseNumber = Some("585"),
      address = Some("Keizersgracht"),
      zipCode = Some("1017 DR"),
      city = Some("Amsterdam"),
      country = "NL",
      chargePointType = "AC",
      connectorType = Connector(
        connectorStandard = ConnectorStandard.`TESLA-R`,
        connectorFormat = ConnectorFormat.Socket),
      maxSocketPower = 16,
      productType = Some("wtf"),
      meterId = Some("1234"),
      chargingPeriods = List(
        CdrPeriod(
          startDateTime = DateTimeNoMillis("2014-08-08T10:10:10+01:00"),
          endDateTime = DateTimeNoMillis("2014-08-08T18:10:10+01:00"),
          billingItem = BillingItem.withName("power"),
          billingValue = 1,
          currency = "EUR",
          itemPrice = 6,
          periodCost = Some(5))))

    cdrInfoToCdr(cdrToCdrInfo(cdr)) mustEqual cdr
  }

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
      authMethods = List(AuthMethod.RfidMifareCls),
      connectors = List(Connector(ConnectorStandard.`TESLA-R`,ConnectorFormat.Socket)),
      operatingTimes = Some(Hours(
        regularHours = List(RegularHours(
          weekday = 1,
          periodBegin = TimeNoSecs("08:00"),
          periodEnd = TimeNoSecs("18:00"))),
        exceptionalOpenings = List(),
        exceptionalClosings = List())))

    cpInfoToChargePoint(chargePointToCpInfo(cp)) must beSome(cp)
  }

  "empty coordinates are not allowed" >> {
    GeoPoint("", "") must throwA[IllegalArgumentException]
  }

  "several number formats are supported for coordinates" >> {
    GeoPoint.fmt(-123.1234567) === "-123.123457"
    GeoPoint.fmt(123.1234567)  === "123.123457"
    GeoPoint.fmt(-0.0004567) === "-0.000457"
    GeoPoint.fmt(.0004567) === "0.000457"
  }
}