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
 * test/resources/soapui/E-Clearing-soapui-project.xml (SoauUI v5.0.0) *
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
      result.success must beTrue
    }

    " confirm CDRs" >> {
      val result = client.confirmCdrs(Seq(cdr1), Seq(cdr2))
      result.success must beTrue
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
      result.success must beTrue
    }

    " return an error for rejected roamingAuthorisationList" >> {
      val tokens = List(token2)
      val result = client.setRoamingAuthorisationList(tokens)
      result.success must beFalse
    }

    " receive roamingAuthorisationListUpdate" >> {
      val authList = client.roamingAuthorisationListUpdate(DateTimeNoMillis("2014-07-14T00:00:00Z"))
      val tokens = authList
      tokens.length === 2
      tokens(0).contractId === "YYABCC00000001"
    }

    " send roamingAuthorisationListUpdate" >> {
      val tokens = List(token2)
      val result = client.setRoamingAuthorisationListUpdate(tokens)
      result.success must beTrue
    }


    " receive chargepointList" >> {
      val cps = client.chargePointList()
      cps(0).evseId === chargePoint1.evseId
    }

    " set charge point list" >> {
      val result = client.setChargePointList(Seq(chargePoint1))
      result.success must beTrue
    }

    " receive chargepointListUpdate" >> {
      val cps = client.chargePointListUpdate(DateTimeNoMillis("2014-07-14T00:00:00Z"))
      cps(0).evseId === "DE*823*E1234*7890"
    }

    " set charge point list update" >> {
      val result = client.setChargePointListUpdate(Seq(chargePoint1))
      result.success must beTrue
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
          "DE*823*E1234*5678",
          EvseStatusMajor.available,
          Some(EvseStatusMinor.reserved)
        ),
        EvseStatus(
          "DE*823*E1234*6789",
          EvseStatusMajor.`not-available`,
          Some(EvseStatusMinor.blocked)
        )
      )
      val result = liveClient.updateStatus(evseStats, Some(DateTimeNoMillis("2014-07-14T00:00:00Z")))
      result.success must beTrue
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
