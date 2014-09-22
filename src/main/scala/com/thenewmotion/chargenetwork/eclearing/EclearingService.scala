package com.thenewmotion.chargenetwork.eclearing

import com.thenewmotion.chargenetwork.eclearing.api.{CDR, ChargeToken, ChargePoint, EclearingApi}
import com.thenewmotion.chargenetwork.eclearing.client.{EclearingClient, Result}
import com.thenewmotion.time.Imports._


/**
 * @author Yaroslav Klymko
 * @author Christoph Zwirello
 */
trait EclearingService extends EclearingApi {
  def client: EclearingClient

  def sendAllTokens(tokens: List[ChargeToken]): Result[ChargeToken] = client.setRoamingAuthorisationList(tokens)
  def recvAllTokens():List[ChargeToken] = client.roamingAuthorisationList()
  def sendNewTokens(tokens: List[ChargeToken]): Result[ChargeToken] = client.setRoamingAuthorisationListUpdate(tokens)
  def recvNewTokens(lastUpdate: DateTime):List[ChargeToken] = client.roamingAuthorisationListUpdate(lastUpdate)

  def sendAllChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint] = client.setChargePointList(chargePoints)
  def recvAllChargePoints():List[ChargePoint] = client.chargePointList()
  def sendNewChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint] = client.setChargePointListUpdate(chargePoints)
  def recvNewChargePoints(lastUpdate: DateTime):List[ChargePoint] = client.chargePointListUpdate(lastUpdate)

  def sendCdrs(cdrs: List[CDR]): Result[CDR] = client.addCdrs(cdrs)
  def recvCdrs():List[CDR] = client.getCdrs()
  def confCdrs(approvedCdrs: List[CDR], declinedCdrs: List[CDR]) = client.confirmCdrs(approvedCdrs, declinedCdrs)
}