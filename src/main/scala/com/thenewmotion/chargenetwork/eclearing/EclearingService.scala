package com.thenewmotion.chargenetwork.eclearing

import com.thenewmotion.chargenetwork.eclearing.api.{CDR, Card, ChargePoint, EclearingApi}
import com.thenewmotion.chargenetwork.eclearing.client.{EclearingClient, Result}
import com.thenewmotion.time.Imports._


/**
 * @author Yaroslav Klymko
 * @author Christoph Zwirello
 */
trait EclearingService extends EclearingApi {
  def client: EclearingClient

  def sendAllCards(cards: List[Card]): Result[Card] = client.setRoamingAuthorisationList(cards)
  def recvAllCards():List[Card] = client.roamingAuthorisationList()
  def sendNewCards(cards: List[Card]): Result[Card] = client.setRoamingAuthorisationListUpdate(cards)
  def recvNewCards(lastUpdate: DateTime):List[Card] = client.roamingAuthorisationListUpdate()

  def sendAllChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint] = client.setChargePointList(chargePoints)
  def recvAllChargePoints():List[ChargePoint] = client.chargePointList()
  def sendNewChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint] = client.setChargePointListUpdate(chargePoints)
  def recvNewChargePoints(lastUpdate: DateTime):List[ChargePoint] = client.chargePointListUpdate()

  def sendCdrs(cdrs: List[CDR]): Result[CDR] = client.addCdrs(cdrs)
  def recvCdrs():List[CDR] = client.getCdrs()
  def confCdrs(approvedCdrs: List[CDR], declinedCdrs: List[CDR]) = client.confirmCdrs(approvedCdrs, declinedCdrs)
}