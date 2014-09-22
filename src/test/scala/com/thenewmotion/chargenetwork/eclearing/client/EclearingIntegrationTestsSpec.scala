package com.thenewmotion.chargenetwork.eclearing.client

import com.thenewmotion.chargenetwork.eclearing.api._
import com.thenewmotion.chargenetwork.eclearing.{EclearingConfig}
import com.typesafe.config._
import org.joda.time.DateTime
import org.specs2.mutable.SpecificationWithJUnit


/**
 * This class is meant for manual tests during the integration
 * tests with Eclearing.
 *
 * @author Christoph Zwirello
 */
class EclearingIntegrationTestsSpec extends SpecificationWithJUnit with CardIntegrationTestScope{
  args(sequential = true)


  "EclearingClient for integration tests" should {


    " receive cdrs" >> {
      val cdrs = client.getCdrs()
      cdrs(0).cdrId === "123456someId123456"
    }
//
//    " add CDRs" >> {
//      val result = client.addCdrs(Seq(cdr1))
//      result.resultCode === "ok"
//    }
//
//    " confirm CDRs" >> {
//      val result = client.confirmCdrs(Seq(cdr1), Seq(cdr2))
//      result.resultCode === "ok"
//    }
//
//
//    " receive roamingAuthorisationList" >> {
//      val authList = client.roamingAuthorisationList()
//      val cards = authList
//      cards.length === 7
//      cards(0).contractId === "YYABCC00000003"
//    }

//    " send roamingAuthorisationList" >> {
//      val cards = List(card1, card2, card3)
//      val result = client.setRoamingAuthorisationList(cards)
//      result.resultCode === "ok"
//    }


//    " receive roamingAuthorisationListUpdate" >> {
//      val authList = client.roamingAuthorisationListUpdate(DateTimeNoMillis("2014-07-14T00:00:00Z"))
//      val cards = authList
//      cards.length === 2
//      cards(0).contractId === "YYABCC00000001"
//    }

//    " send roamingAuthorisationListUpdate" >> {
//      val cards = List(card1, card2, card3)
//      val result = client.setRoamingAuthorisationListUpdate(cards)
//      println(result.resultCode)
//      println(result.resultDescription)
//      println(result.resultPayload)
//      result.resultCode === "ok"
//    }


//    " receive chargepointList" >> {
//      val cps = client.chargePointList()
//      cps(0).evseId === chargePoint1.evseId
//    }
//
//    " set charge point list" >> {
//      val result = client.setChargePointList(Seq(chargePoint1))
//      result.resultCode === "ok"
//    }
//
//    " receive chargepointListUpdate" >> {
//      val cps = client.chargePointListUpdate(DateTimeNoMillis("2014-07-14T00:00:00Z"))
//      cps(0).evseId === "DE*823*E1234*7890"
//    }
//
//    " set charge point list update" >> {
//      val result = client.setChargePointListUpdate(Seq(chargePoint1))
//      result.resultCode === "ok"
//    }

  }

//  "Eclearing live client" should {
//
//    val conf: Config = ConfigFactory.load()
//
//    val liveClient = EclearingClient.createCxfLiveClient(
//      new EclearingConfig(
//        "",
//        conf.getString("e-clearing.live-service-uri"),
//        conf.getString("e-clearing.user"),
//        conf.getString("e-clearing.password"))
//    )
//
//    " update evse status" >> {
//      val evseStats = List(
//        EvseStatus(
//          "DE*823*E1234*5678",
//          EvseStatusMajor.available,
//          Some(EvseStatusMinor.reserved)
//        ),
//        EvseStatus(
//          "DE*823*E1234*6789",
//          EvseStatusMajor.`not-available`,
//          Some(EvseStatusMinor.blocked)
//        )
//      )
//      val result = liveClient.updateStatus(evseStats, Some(DateTimeNoMillis("2014-07-14T00:00:00Z")))
//      result.resultCode === "ok"
//    }
//  }
}


trait CardIntegrationTestScope {

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

  val card1 = Card(
    contractId = "DE-TNM-000000001",
    emtId=EmtId(
      tokenType = TokenType.rfid,
//      tokenSubType = Some(TokenSubType.MifareCl),
      tokenId = "C1"),
    printedNumber = Some("Number 01"),
    expiryDate = new DateTime()
  )

  val card2 = Card(
    contractId = "DE-TNM-000000002",
    emtId=EmtId(
      tokenType = TokenType.rfid,
//      tokenSubType = Some(TokenSubType.MifareCl),
      tokenId = "C2"),
    printedNumber = Some("Number 02"),
    expiryDate = new DateTime().plusDays(1)
  )

  val card3 = Card(
    contractId = "DE-TNM-000000003",
    emtId=EmtId(
      tokenType = TokenType.rfid,
//      tokenSubType = Some(TokenSubType.MifareCl),
      tokenId = "C3"),
    printedNumber = Some("Number 03"),
    expiryDate = new DateTime().plusYears(1)
  )
}


