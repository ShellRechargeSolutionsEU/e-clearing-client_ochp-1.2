package com.thenewmotion.chargenetwork.eclearing

import api._
import eu.ochp._1.{DateTimeType, EmtId, RoamingAuthorisationInfo}


/**
 * @author Yaroslav Klymko
 */
object Converters{
  implicit def roamingAuthorisationInfoToCard(rai: RoamingAuthorisationInfo): Card = {
    Card(
      evcoId = rai.getContractId,
      tokenSubType = TokenSubType.withName(rai.getEmtId.getTokenSubType),
      tokenId = rai.getEmtId.getInstance,
      printedNumber = rai.getPrintedNumber,
      expiryDate = ExpiryDate(rai.getExpiryDate.getDateTime)
    )
  }

  implicit def cardToRoamingAuthorisationInfo(card: Card): RoamingAuthorisationInfo = {
    import card._

    val rai = new RoamingAuthorisationInfo()
    val emtId = new EmtId()
    rai.setContractId(evcoId)
    emtId.setInstance(tokenId)
    emtId.setTokenType("rfid")
    emtId.setTokenSubType(tokenSubType.toString)
    rai.setEmtId(emtId)
    val expDate = new DateTimeType()
    expDate.setDateTime(expiryDate.getOrElse("").toString)
    rai.setExpiryDate(expDate)
    rai
  }
}
