package com.thenewmotion.chargenetwork.eclearing.client

import com.thenewmotion.chargenetwork.eclearing.api._
import com.thenewmotion.chargenetwork.eclearing.{EclearingConfig}
import com.typesafe.config._
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope


/**
 * EclearingClient integration tests.
 * need a soapUI mock service running on 8088.
 * The respective soapui project can be found in
 * test/resources/soapui/E-Clearing-soapui-project.xml (SoauUI v5.1.2;
 * but seem to work with 5.0.0, too)
 *
 * The mock service will be starting automatically during the pre-integration-test phase
 * so this test can run during integration-test phase
 *
 *
 * @author Christoph Zwirello
 */
class EclearingClientSpecIT extends SpecificationWithJUnit with TokenTestScope with CpTestScope
with CdrTestScope{
  args(sequential = true)


  "EclearingClient" should {


    " receive cdrs" >> {
      val cdrs = client.getCdrs()
      cdrs(0).cdrId === "123456someId123456"
    }

    " add CDRs" >> {
      val result = client.addCdrs(Seq(cdr1))
      result.status === ResultCode.success
    }

    " confirm CDRs" >> {
      val result = client.confirmCdrs(Seq(cdr1), Seq(cdr2))
      result.status === ResultCode.success
    }


    " receive roamingAuthorisationList" >> {
      val authList = client.roamingAuthorisationList()
      val tokens = authList
      tokens.length === 7
      tokens(0).contractId === "YYABCC00000003"
    }

    " send roamingAuthorisationList" >> {
      val tokens = List(token1)
      val rais = tokens
      val result = client.setRoamingAuthorisationList(rais)
      result.status === ResultCode.success
    }

    " return an error for rejected roamingAuthorisationList" >> {
      val tokens = List(token2)
      val result = client.setRoamingAuthorisationList(tokens)
      result.status === ResultCode.failure
    }

    " receive roamingAuthorisationListUpdate" >> {
      val authList = client.roamingAuthorisationListUpdate(DateTimeNoMillis("2014-07-14T00:00:00Z"))
      val tokens = authList
      tokens.length === 10
      tokens(0).contractId === "YYABCC00000003"
    }

    " send roamingAuthorisationListUpdate" >> {
      val tokens = List(token2)
      val result = client.setRoamingAuthorisationListUpdate(tokens)
      result.status === ResultCode.success
    }


    " receive chargepointList" >> {
      val result = client.chargePointList()
      result.items(0).evseId === chargePoint1.evseId
    }

    " set charge point list" >> {
      val result = client.setChargePointList(Seq(chargePoint1))
      result.status === ResultCode.success
    }

    " receive chargepointListUpdate" >> {
      val result = client.chargePointListUpdate(DateTimeNoMillis("2014-07-14T00:00:00Z"))
      result.items(0).evseId === EvseId("DE*823*E1234*7890")
    }

    " set charge point list update" >> {
      val result = client.setChargePointListUpdate(Seq(chargePoint1))
      result.status === ResultCode.success
    }

  }

  "Eclearing live client" should {

    val conf: Config = ConfigFactory.load()

    val liveClient = EclearingClient.createCxfLiveClient(
      new EclearingConfig(
        "",
        conf.getString("e-clearing.live-service-uri"),
        conf.getString("e-clearing.user"),
        conf.getString("e-clearing.password"))
    )

    " update evse status" >> {
      val evseStats = List(
        EvseStatus(
          EvseId("DE*823*E1234*5678"),
          EvseStatusMajor.available,
          Some(EvseStatusMinor.reserved)
        ),
        EvseStatus(
          EvseId("DE*823*E1234*6789"),
          EvseStatusMajor.`not-available`,
          Some(EvseStatusMinor.blocked)
        )
      )
      val result = liveClient.updateStatus(evseStats, Some(DateTimeNoMillis("2014-07-14T00:00:00Z")))
      result.status === ResultCode.success
    }
  }
}



trait TokenTestScope extends Scope{

  val conf: Config = ConfigFactory.load()

  val client = EclearingClient.createCxfClient(
    new EclearingConfig(
      conf.getString("e-clearing.service-uri"),
      "",
      conf.getString("e-clearing.user"),
      conf.getString("e-clearing.password"))
  )

  val faultyClient = EclearingClient.createCxfClient(
    new EclearingConfig(
      "http://localhost:8",
      "",
      conf.getString("e-clearing.user"),
      conf.getString("e-clearing.password"))
  )

  val token1 = ChargeToken(
    contractId = "YYABCC00000003",
    emtId=EmtId(
      tokenSubType = Some(TokenSubType.withName("mifareCls")),
      tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0"),
    printedNumber = Some("YYABCC00000003J"),
    expiryDate = DateTimeNoMillis("2014-07-14T02:00:00+02:00")
  )

  val token2 = ChargeToken(
    contractId = "YYABCC00000003",
    emtId=EmtId(
      tokenSubType = Some(TokenSubType.withName("mifareCls")),
      tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA1"), // cf. last digit!)
    printedNumber = Some("YYABCC00000003J"),

    expiryDate = DateTimeNoMillis("2014-07-14T00:00:00Z")
  )
}
