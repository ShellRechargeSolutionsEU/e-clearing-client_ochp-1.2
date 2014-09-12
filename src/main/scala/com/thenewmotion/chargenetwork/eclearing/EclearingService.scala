package com.thenewmotion.chargenetwork.eclearing

import com.thenewmotion.chargenetwork.eclearing.api.{ChargePoint, CDR, EclearingApi, Card}
import com.thenewmotion.chargenetwork.eclearing.client.{Result, EclearingClient}
import com.thenewmotion.time.Imports._
import eu.ochp._1.RoamingAuthorisationInfo


/**
 * @author Yaroslav Klymko
 */
trait EclearingService extends EclearingApi {
  def client: EclearingClient

  def sendAllCards(cards: List[Card]): Result = client.setRoamingAuthorisationList(cards)
  def recvAllCards():List[Card] = client.roamingAuthorisationList()
  def sendNewCards(cards: List[Card]): Result = client.setRoamingAuthorisationListUpdate(cards)
  def recvNewCards(lastUpdate: DateTime):List[Card] = client.roamingAuthorisationListUpdate()

  def sendAllChargePoints(chargePoints: List[ChargePoint]): Result = client.setChargePointList(chargePoints)
  def recvAllChargePoints():List[ChargePoint] = client.chargePointList()
  def sendNewChargePoints(chargePoints: List[ChargePoint]): Result = client.setChargePointListUpdate(chargePoints)
  def recvNewChargePoints(lastUpdate: DateTime):List[ChargePoint] = client.chargePointListUpdate()

  def sendCdrs(cdrs: List[CDR]): Result = client.addCdrs(cdrs)
  def recvCdrs():List[CDR] = client.getCdrs()
}