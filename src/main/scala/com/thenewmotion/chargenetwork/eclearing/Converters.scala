package com.thenewmotion.chargenetwork.eclearing

import api._
import com.thenewmotion.chargenetwork.eclearing.api.BillingItemType
import com.thenewmotion.chargenetwork.eclearing.api.CdrPeriodType
import com.thenewmotion.chargenetwork.eclearing.api.ConnectorFormat
import com.thenewmotion.chargenetwork.eclearing.api.ConnectorStandard
import eu.ochp._1.{ConnectorType => GenConnectorType}
import eu.ochp._1.{EmtId => GenEmtId, CdrStatusType => GenCdrStatusType,
ConnectorFormat => GenConnectorFormat,
ConnectorStandard => GenConnectorStandard,
CdrPeriodType=>GenCdrPeriodType,
BillingItemType => GenBillingItemType,
RoamingAuthorisationInfo, CDRInfo, DateTimeType, LocalDateTimeType}


/**
 * @author Yaroslav Klymko
 */
object Converters{
  import scala.collection.JavaConverters._

  implicit def roamingAuthorisationInfoToCard(rai: RoamingAuthorisationInfo): Card = {
    Card(
      contractId = rai.getContractId,
      emtId = EmtId(
        tokenId = rai.getEmtId.getInstance,
        tokenType = TokenType.withName(rai.getEmtId.getTokenType),
        tokenSubType = Some(TokenSubType.withName(rai.getEmtId.getTokenSubType))),
      printedNumber = Some(rai.getPrintedNumber),
      expiryDate = ExpiryDate(rai.getExpiryDate.getDateTime)
    )
  }

  implicit def cardToRoamingAuthorisationInfo(card: Card): RoamingAuthorisationInfo = {
    import card._

    val rai = new RoamingAuthorisationInfo()
    val emtId = new GenEmtId()
    rai.setContractId(contractId)
    emtId.setInstance(card.emtId.tokenId)
    emtId.setTokenType(card.emtId.tokenType.toString)
    card.emtId.tokenSubType map {st => emtId.setTokenSubType(st.toString)}
    emtId.setRepresentation("plain")
    rai.setEmtId(emtId)
    val expDate = new DateTimeType()
    expDate.setDateTime(expiryDate.toString)
    rai.setExpiryDate(expDate)
    rai
  }

  def toOption (value: String):Option[String] = {
    value match {case null => None; case s if !s.isEmpty => Some(s); case _ => None}
  }

  implicit def cdrInfoToCdr(cdrinfo: CDRInfo): CDR = {
    CDR(
      cdrId = cdrinfo.getCdrId,
      evseId = cdrinfo.getEvseId,
      emtId = EmtId(
        tokenId = cdrinfo.getEmtId.getInstance,
        tokenType = TokenType.withName(cdrinfo.getEmtId.getTokenType),
        tokenSubType = Some(TokenSubType.withName(cdrinfo.getEmtId.getTokenSubType))
      ),
      contractId = cdrinfo.getContractId,
      liveAuthId = toOption(cdrinfo.getLiveAuthId),
      status = CdrStatusType.withName(cdrinfo.getStatus.getCdrStatusType),
      startDateTime = CdrStartDateTime(cdrinfo.getStartDateTime.getLocalDateTime),
      endDateTime = CdrEndDateTime(cdrinfo.getEndDateTime.getLocalDateTime),
      duration = toOption(cdrinfo.getDuration),
      houseNumber = toOption(cdrinfo.getHouseNumber),
      address = toOption(cdrinfo.getAddress),
      zipCode = toOption(cdrinfo.getZipCode),
      city = toOption(cdrinfo.getCity),
      country = cdrinfo.getCountry,
      chargePointType = cdrinfo.getChargePointType,
      connectorType = ConnectorType(
        connectorStandard = ConnectorStandard.withName(
          cdrinfo.getConnectorType.getConnectorStandard.getConnectorStandard),
        connectorFormat = ConnectorFormat.withName(
          cdrinfo.getConnectorType.getConnectorFormat.getConnectorFormat)),
      maxSocketPower = cdrinfo.getMaxSocketPower,
      productType = toOption(cdrinfo.getProductType),
      meterId = toOption(cdrinfo.getMeterId),
      chargingPeriods = cdrinfo.getChargingPeriods.asScala.toList.map( cdrPeriod=>
        CdrPeriodType(
          startDateTime = CdrStartDateTime(cdrPeriod.getStartDateTime.getLocalDateTime),
          endDateTime = CdrEndDateTime(cdrPeriod.getEndDateTime.getLocalDateTime),
          billingItem = BillingItemType.withName(cdrPeriod.getBillingItem.getBillingItemType) ,
          billingValue = cdrPeriod.getBillingValue,
          currency = cdrPeriod.getCurrency,
          itemPrice = cdrPeriod.getItemPrice,
          periodCost = Some(cdrPeriod.getPeriodCost)
        )

      )
    )
  }



  implicit def cdrToCdrInfo(cdr: CDR): CDRInfo = {
    import cdr._
    val cdrInfo = new CDRInfo
    cdr.address match {case Some(s) if !s.isEmpty => cdrInfo.setAddress(s)}
    cdrInfo.setCdrId(cdr.cdrId)
    cdrInfo.setChargePointType(cdr.chargePointType)

    val cType = new GenConnectorType()
    val cFormat = new GenConnectorFormat()
    cFormat.setConnectorFormat(cdr.connectorType.connectorFormat.toString)
    cType.setConnectorFormat(cFormat)
    val cStandard = new GenConnectorStandard()
    cStandard.setConnectorStandard(cdr.connectorType.connectorStandard.toString)
    cType.setConnectorStandard(cStandard)
    cdrInfo.setConnectorType(cType)
    cdrInfo.setContractId(cdr.contractId)
    cdr.houseNumber match {case Some(s) if !s.isEmpty => cdrInfo.setHouseNumber(s)}
    cdr.zipCode match {case Some(s) if !s.isEmpty => cdrInfo.setZipCode(s)}
    cdr.city match {case Some(s) if !s.isEmpty => cdrInfo.setCity(s)}
    cdrInfo.setCountry(cdr.country)
    cdr.duration  match {case Some(s) if !s.isEmpty => cdrInfo.setDuration(s)}
    val eid = new GenEmtId()
    eid.setInstance(cdr.emtId.tokenId)
    eid.setTokenType(cdr.emtId.tokenType.toString)
    eid.setTokenSubType(cdr.emtId.tokenSubType.toString)
    cdrInfo.setEmtId(eid)
    val start = new LocalDateTimeType()
    start.setLocalDateTime(startDateTime.toString)
    cdrInfo.setStartDateTime(start)
    val end = new LocalDateTimeType()
    end.setLocalDateTime(endDateTime.toString)
    cdrInfo.setEndDateTime(end)
    cdrInfo.setEvseId(cdr.evseId)

    cdr.liveAuthId match {case Some(s) if !s.isEmpty => cdrInfo.setLiveAuthId(s)}
    cdrInfo.setMaxSocketPower(cdr.maxSocketPower)
    cdr.meterId match {case Some(s) if !s.isEmpty => cdrInfo.setMeterId(s)}
    cdr.productType match {case Some(s) if !s.isEmpty => cdrInfo.setProductType(s)}

    val cdrStatus = new GenCdrStatusType()
    cdrStatus.setCdrStatusType(cdr.status.toString)
    cdrInfo.setStatus(cdrStatus)
    cdrInfo.getChargingPeriods.addAll(
      cdr.chargingPeriods.map {chargePeriodToGenCp} asJavaCollection)
    cdrInfo
  }

  def chargePeriodToGenCp(gcp: CdrPeriodType): GenCdrPeriodType = {
    val period1 = new GenCdrPeriodType()
    val start = new LocalDateTimeType()
    start.setLocalDateTime(gcp.startDateTime.toString)
    period1.setStartDateTime(start)
    val end = new LocalDateTimeType()
    end.setLocalDateTime(gcp.endDateTime.toString)
    period1.setEndDateTime(end)
    val billingItem = new GenBillingItemType()
    billingItem.setBillingItemType(gcp.billingItem.toString)
    period1.setBillingItem(billingItem)
    period1.setBillingValue(gcp.billingValue)
    period1.setCurrency(gcp.currency)
    period1.setItemPrice(gcp.itemPrice)
    gcp.periodCost.foreach {period1.setPeriodCost(_)}
    period1
  }
}
