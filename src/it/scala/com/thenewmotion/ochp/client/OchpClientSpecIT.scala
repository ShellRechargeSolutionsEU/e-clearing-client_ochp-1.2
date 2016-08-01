package com.thenewmotion.ochp
package client

import api._
import org.specs2.mutable.Specification
import org.specs2.specification.Scope


/**
 * OchpClient integration tests.
 * need a soapUI mock service running on 8088.
 * The respective soapui project can be found in
 * it/resources/soapui/E-Clearing-soapui-project.xml (SoauUI v5.1.2;
 * but seem to work with 5.0.0, too)
 *
 * The mock service will be starting automatically during the pre-integration-test phase
 * so this test can run during integration-test phase
 *
 */
class OchpClientSpecIT extends Specification {
  args(sequential = true)

  "OCHP Client" should {

    " receive cdrs" >> new TestScope {
      val cdrs = client.getCdrs()
      cdrs.head.cdrId === "YAABC123"
    }

    " add CDRs" >> new TestCdrScope {
      val result = client.addCdrs(Seq(cdr1))
      result.status === ResultCode.success
    }

    " confirm CDRs" >> new TestCdrScope {
      val result = client.confirmCdrs(Seq(cdr1), Seq(cdr2))
      result.status === ResultCode.success
    }


    " receive roamingAuthorisationList" >> new TestScope {
      val authList = client.roamingAuthorisationList()
      val tokens = authList
      tokens.length === 7
      tokens.head.contractId === "YYABCC00000003"
    }

    " send roamingAuthorisationList" >> new TestScope {
      val tokens = List(token)
      val rais = tokens
      val result = client.setRoamingAuthorisationList(rais)
      result.status === ResultCode.success
    }

    " return an error for rejected roamingAuthorisationList" >> new TestScope {
      /* See script of SetRoamingAuthorisationList in E-Clearing-soapui-project.xml */
      val rejectedToken = token.copy(
        emtId = token.emtId.copy(
          tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA1"))
      val result = client.setRoamingAuthorisationList(List(rejectedToken))
      result.status === ResultCode.failure
    }

    " receive roamingAuthorisationListUpdate" >> new TestScope {
      val authList = client.roamingAuthorisationListUpdate(DateTimeNoMillis("2014-07-14T00:00:00Z"))
      val tokens = authList
      tokens.length === 10
      tokens.head.contractId === "YYABCC00000003"
    }

    " send roamingAuthorisationListUpdate" >> new TestScope {
      /* See script of UpdateRoamingAuthorisationList in E-Clearing-soapui-project.xml*/
      val tokens = List(
        token.copy(
          emtId = token.emtId.copy(
            tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA1")))
      val result = client.setRoamingAuthorisationListUpdate(tokens)
      result.status === ResultCode.success
    }


    " receive chargepointList" >> new TestChargePointScope {
      val result = client.chargePointList()
      result.items.head.evseId === chargePoint1.evseId
    }

    " set charge point list" >> new TestChargePointScope {
      val result = client.setChargePointList(Seq(chargePoint1))
      result.status === ResultCode.success
    }

    " receive chargepointListUpdate" >> new TestScope {
      val result = client.chargePointListUpdate(DateTimeNoMillis("2014-07-14T00:00:00Z"))
      result.items.head.evseId === EvseId("DE*823*E1234*7890")
    }

    " set charge point list update" >> new TestChargePointScope {
      val result = client.setChargePointListUpdate(Seq(chargePoint1))
      result.status === ResultCode.success
    }

  }

  trait TestScope extends Scope {

    val conf = new OchpConfig(
      wsUri = "http://localhost:8088/mockeCHS-OCHP_1.3",
      liveWsUri = "http://localhost:8088/mockeCHS-OCHP_1.3/live",
      user = "backend.tnm",
      password = "123456")

    val client = OchpClient.createCxfClient(conf)

    val token = ChargeToken(
      contractId = "YYABCC00000003",
      emtId=EmtId(
        tokenSubType = Some(TokenSubType.withName("mifareCls")),
        tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0"),
      printedNumber = Some("YYABCC00000003J"),
      expiryDate = DateTimeNoMillis("2014-07-14T02:00:00+02:00")
    )
  }

  trait TestCdrScope extends TestScope {
    val cdr1 = CDR(
      cdrId = "123456someId123456",
      evseId = "FR*A23*E45B*78C",
      emtId = EmtId(
        tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0",
        tokenType = TokenType.withName("rfid"),
        tokenSubType = Some(TokenSubType.withName("mifareCls"))
      ),
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
          periodCost = Some(5)
        )))

    val cdr2 = CDR(
      cdrId = "123456someId123457",
      evseId = "FR*A23*E45B*78C",
      emtId = EmtId(
        tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA1",
        tokenType = TokenType.withName("rfid"),
        tokenSubType = Some(TokenSubType.withName("mifareCls"))
      ),
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
          periodCost = Some(5)
        )))
  }

  trait TestChargePointScope extends TestScope {
    val chargePoint1 = ChargePoint(
      evseId = EvseId("DE*823*E1234*5678"),
      locationId = "WERELD",
      locationName = "Keizersgracht-585",
      locationNameLang = "NLD",
      address = CpAddress(
        address = "Keizersgracht 585",
        city = "Amsterdam",
        zipCode = "1017DR",
        country = "NLD"
      ),
      chargePointLocation = GeoPoint(
        lat = 52.364208,
        lon = 4.891792
      ),
      authMethods = List(AuthMethod.RfidMifareCls),
      connectors = List(Connector(ConnectorStandard.`TESLA-R`,ConnectorFormat.Socket)),
      operatingTimes = Some(Hours(
        regularHoursOrTwentyFourSeven = Left(List(RegularHours(
          weekday = 1,
          periodBegin = TimeNoSecs("08:00"),
          periodEnd = TimeNoSecs("18:00")
        ))),
        exceptionalOpenings = List(),
        exceptionalClosings = List()))
    )
  }

}
