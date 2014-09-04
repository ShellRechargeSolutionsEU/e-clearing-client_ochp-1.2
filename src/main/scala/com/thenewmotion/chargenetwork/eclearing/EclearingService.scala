package com.thenewmotion.chargenetwork.eclearing

import api.{EclearingApi, Card}
import com.thenewmotion.chargenetwork.eclearing.client.{Result, EclearingClient}
import eu.ochp._1.RoamingAuthorisationInfo


/**
 * @author Yaroslav Klymko
 */
trait EclearingService extends EclearingApi {
  def client: EclearingClient

  import Converters._
  def sendCards(cards: Seq[Card]): Result = client.setRoamingAuthorisationList(cards.map(implicitly[RoamingAuthorisationInfo](_)))
  def recvCards() = client.roamingAuthorisationList().map(implicitly[Card](_))
}