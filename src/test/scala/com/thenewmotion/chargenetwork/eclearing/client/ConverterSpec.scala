package com.thenewmotion.chargenetwork.eclearing.client

import com.thenewmotion.chargenetwork.eclearing.Converters._
import com.thenewmotion.chargenetwork.eclearing.api.BillingItem
import com.thenewmotion.chargenetwork.eclearing.api.CdrPeriod
import com.thenewmotion.chargenetwork.eclearing.api._
import eu.ochp._1
import eu.ochp._1.{EmtId => GenEmtId, CdrStatusType => GenCdrStatusType, ConnectorFormat => GenConnectorFormat, ConnectorStandard => GenConnectorStandard, ConnectorType => GenConnectorType, _}
import org.specs2.mutable.SpecificationWithJUnit

/**
 * Created with IntelliJ IDEA.
 * User: czwirello
 * Date: 08.09.14
 */
class ConverterSpec extends SpecificationWithJUnit with CpTestScope{
   "Converter " should {
     " translate CDRinfo into CDR" >> {
       val cdrinfo = new CDRInfo()
       val emtId = new GenEmtId()
       cdrinfo.setContractId("DE-LND-C00001516-E")
       emtId.setInstance("96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0")
       emtId.setTokenType("rfid")
       emtId.setTokenSubType("mifareCls")
       emtId.setRepresentation("plain")
       cdrinfo.setEmtId(emtId)
       val status = new GenCdrStatusType()
        status.setCdrStatusType("new")
       cdrinfo.setStatus(status)

       val startDate = new LocalDateTimeType()
       startDate.setLocalDateTime("2014-08-08T10:10:10+01:00")
       cdrinfo.setStartDateTime(startDate)
       val endDate = new LocalDateTimeType()
       endDate.setLocalDateTime("2014-08-08T18:10:10+01:00")
       cdrinfo.setEndDateTime(endDate)
       cdrinfo.setConnectorType(teslaSocketConnector)



       val cdr:CDR = cdrInfoToCdr(cdrinfo)
       cdr.contractId === "DE-LND-C00001516-E"
       cdr.emtId.tokenId === "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0"
       cdr.emtId.tokenType.toString === "rfid"
       cdr.emtId.tokenSubType.get.toString === "mifareCls"
       cdr.status.toString === "new"
       cdr.maxSocketPower === 0
       cdr.liveAuthId === None
       cdr.startDateTime === DateTimeNoMillis("2014-08-08T10:10:10+01:00")
       cdr.endDateTime === DateTimeNoMillis("2014-08-08T18:10:10+01:00")
       cdr.connectorType.connectorStandard.toString === "TESLA-R"
       cdr.connectorType.connectorFormat.toString === "Socket"
     }

     " translate CDR into CDRInfo" >> {
       val cdr = CDR(
         cdrId = "123456someId123456",
         evseId = "FR*A23*E45B*78C",
         emtId = EmtId(
            tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0",
            tokenType = TokenType.withName("rfid"),
            tokenSubType = Some(TokenSubType.withName("mifareCls"))
           ),
         contractId = "DE-LND-C00001516-E",
         liveAuthId = Some("wtf"),
         status = CdrStatusType.withName("new"),
         startDateTime = DateTimeNoMillis("2014-08-08T10:10:10+01:00"),
         endDateTime = DateTimeNoMillis("2014-08-08T18:10:10+01:00"),
         duration = Some("200"),
         houseNumber = Some("585"),
         address = Some("Keizersgracht"),
         zipCode = Some("1017 DR"),
         city = Some("Amsterdam"),
         country = "NL",
         chargePointType = "AC",
         connectorType = ConnectorType(
           connectorStandard = ConnectorStandard.`TESLA-R`,
           connectorFormat = ConnectorFormat.Socket),
         maxSocketPower = 16,
         productType = Some("wtf"),
         meterId = Some("1234"),
         chargingPeriods = List(
           CdrPeriod(
             startDateTime = DateTimeNoMillis("2014-08-08T10:10:10+01:00"),
             endDateTime = DateTimeNoMillis("2014-08-08T18:10:10+01:00"),
             billingItem = BillingItem.withName("power"),
             billingValue = 1,
             currency = "EUR",
             itemPrice = 6,
             periodCost = Some(5)
           ))


       )
       val cdrinfo: CDRInfo = cdrToCdrInfo(cdr)
       cdrinfo.getCdrId === "123456someId123456"
       cdrinfo.getEvseId === "FR*A23*E45B*78C"
       cdrinfo.getEmtId.getInstance === "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0"
       cdrinfo.getContractId === "DE-LND-C00001516-E"
       cdrinfo.getLiveAuthId === "wtf"
       cdrinfo.getStatus.getCdrStatusType === "new"
       cdrinfo.getStartDateTime.getLocalDateTime === "2014-08-08T11:10:10.000+02:00"
       cdrinfo.getConnectorType.getConnectorStandard.getConnectorStandard === "TESLA-R"
       cdrinfo.getMaxSocketPower === 16
       cdrinfo.getProductType === "wtf"
       import scala.collection.JavaConverters._
       val chargePeriod: _1.CdrPeriodType = cdrinfo.getChargingPeriods.asScala.head
       chargePeriod.getBillingItem.getBillingItemType === "power"
       chargePeriod.getStartDateTime.getLocalDateTime === "2014-08-08T11:10:10.000+02:00"
       chargePeriod.getPeriodCost === 5
       chargePeriod.getItemPrice === 6
     }

     " translate ChargePointInfo into ChargePoint" >> {

       val chargePoint = cpInfoToChargePoint(chargePointInfo1)
       chargePoint.evseId === "DE*823*E1234*5678"
       chargePoint.locationId === "Wereld"
       chargePoint.locationName === ""
       chargePoint.locationNameLang === "NL"
       chargePoint.address.address === "Keizersgracht 585"
       chargePoint.address.city === "Amsterdam"
       chargePoint.authMethods(0) === AuthMethod.RfidMifareCls
       chargePoint.connectors(0).connectorFormat === ConnectorFormat.Socket
       chargePoint.connectors(0).connectorStandard === ConnectorStandard.`TESLA-R`
       chargePoint.operatingTimes.get.regularHours(0).weekday === 1
       chargePoint.operatingTimes.get.regularHours(0).periodBegin === "08:00"
       chargePoint.operatingTimes.get.regularHours(0).periodEnd === "18:00"

     }
   }
}

trait CpTestScope {
  val teslaSocketConnector = new GenConnectorType()
  val connForm = new GenConnectorFormat()
  connForm.setConnectorFormat("Socket")
  val connStandard = new GenConnectorStandard()
  connStandard.setConnectorStandard("TESLA-R")

  teslaSocketConnector.setConnectorFormat(connForm)
  teslaSocketConnector.setConnectorStandard(connStandard)

  val chargePointInfo1 = new ChargePointInfo()
  chargePointInfo1.setEvseId("DE*823*E1234*5678")
  chargePointInfo1.setLocationId("Wereld")
  chargePointInfo1.setLocationName("")
  chargePointInfo1.setLocationNameLang("NL")
  chargePointInfo1.setAddress("Keizersgracht 585")
  chargePointInfo1.setCity("Amsterdam")
  chargePointInfo1.setZipCode("1017DR")
  chargePointInfo1.setCountry("NL")
  val loc = new GeoPointType()
  loc.setLat("52.36420822143555")
  loc.setLon("4.891792297363281")
  chargePointInfo1.setGeoLocation(loc)
  val authType = new AuthMethodType()
  authType.setAuthMethodType("RfidMifareCls")
  chargePointInfo1.getAuthMethods.add(authType)
  chargePointInfo1.getConnectors.add(teslaSocketConnector)
  val operatingTimes = new HoursType()
  val regularHours = new RegularHoursType()
  regularHours.setWeekday(1)
  regularHours.setPeriodBegin("08:00")
  regularHours.setPeriodEnd("18:00")
  operatingTimes.getRegularHours.add(regularHours)
  chargePointInfo1.setOperatingTimes(operatingTimes)
}
