package com.thenewmotion.chargenetwork.eclearing

import com.thenewmotion.chargenetwork.eclearing.api.{CDR, EclearingApi, Card}
import com.thenewmotion.chargenetwork.eclearing.client.{Result, EclearingClient}
import eu.ochp._1.RoamingAuthorisationInfo


/**
 * @author Yaroslav Klymko
 */
trait EclearingService extends EclearingApi {
  def client: EclearingClient

  def sendCards(cards: List[Card]): Result = client.setRoamingAuthorisationList(cards)
  def recvCards():List[Card] = client.roamingAuthorisationList()

  def sendCdrs(cdrs: List[CDR]): Result = client.addCdrs(cdrs)
  def recvCdrs():List[CDR] = client.getCdrs()
}