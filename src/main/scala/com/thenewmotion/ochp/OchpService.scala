package com.thenewmotion.ochp

import api._
import client.{Result, OchpClient, OchpLiveClient}
import com.thenewmotion.time.Imports._

trait OchpService extends OchpApi {
  def client: OchpClient

  def sendAllTokens(tokens: List[ChargeToken]): Result[ChargeToken] = client.setRoamingAuthorisationList(tokens)
  def recvAllTokens():List[ChargeToken] = client.roamingAuthorisationList()
  def sendNewTokens(tokens: List[ChargeToken]): Result[ChargeToken] = client.setRoamingAuthorisationListUpdate(tokens)
  def recvNewTokens(lastUpdate: DateTime):List[ChargeToken] = client.roamingAuthorisationListUpdate(lastUpdate)

  def sendAllChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint] = client.setChargePointList(chargePoints)
  def recvAllChargePoints():Result[ChargePoint] = client.chargePointList()
  def sendNewChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint] = client.setChargePointListUpdate(chargePoints)
  def recvNewChargePoints(lastUpdate: DateTime):Result[ChargePoint] = client.chargePointListUpdate(lastUpdate)

  def sendCdrs(cdrs: List[CDR]): Result[CDR] = client.addCdrs(cdrs)
  def recvCdrs():List[CDR] = client.getCdrs()
  def confCdrs(approvedCdrs: List[CDR], declinedCdrs: List[CDR]) = client.confirmCdrs(approvedCdrs, declinedCdrs)
}

trait OchpLiveService {
  def client: OchpLiveClient

  def getStatus(since: Option[DateTime]) = client.getStatus(since)
  def updateStatus(statuses: List[EvseStatus]) = client.updateStatus(statuses)
}
