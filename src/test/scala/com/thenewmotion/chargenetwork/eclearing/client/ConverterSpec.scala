package com.thenewmotion.chargenetwork.eclearing.client

import com.thenewmotion.chargenetwork.eclearing.Converters._
import com.thenewmotion.chargenetwork.eclearing.api._
import eu.ochp._1
import eu.ochp._1.{EmtId => GenEmtId, CdrStatusType => GenCdrStatusType,
ConnectorFormat => GenConnectorFormat,
ConnectorStandard => GenConnectorStandard,
ConnectorType => GenConnectorType,
RoamingAuthorisationInfo, CDRInfo, DateTimeType, LocalDateTimeType}
import org.specs2.mutable.SpecificationWithJUnit

/**
 * Created with IntelliJ IDEA.
 * User: czwirello
 * Date: 08.09.14
 */
class ConverterSpec extends SpecificationWithJUnit{
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

       val cType = new GenConnectorType()
       val standard: GenConnectorStandard = new GenConnectorStandard()
       standard.setConnectorStandard("TESLA-R")
       cType.setConnectorStandard(standard)
       val cformat = new GenConnectorFormat()
       cformat.setConnectorFormat("Socket")
       cType.setConnectorFormat(cformat)
       cdrinfo.setConnectorType(cType)



       val cdr:CDR = cdrInfoToCdr(cdrinfo)
       cdr.contractId === "DE-LND-C00001516-E"
       cdr.emtId.tokenId === "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0"
       cdr.emtId.tokenType.toString === "rfid"
       cdr.emtId.tokenSubType.get.toString === "mifareCls"
       cdr.status.toString === "new"
       cdr.maxSocketPower === 0
       cdr.liveAuthId === None
       cdr.startDateTime === CdrStartDateTime("2014-08-08T10:10:10+01:00")
       cdr.endDateTime === CdrEndDateTime("2014-08-08T18:10:10+01:00")
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
         startDateTime = CdrStartDateTime("2014-08-08T10:10:10+01:00"),
         endDateTime = CdrEndDateTime("2014-08-08T18:10:10+01:00"),
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
           CdrPeriodType(
             startDateTime = CdrStartDateTime("2014-08-08T10:10:10+01:00"),
             endDateTime = CdrEndDateTime("2014-08-08T18:10:10+01:00"),
             billingItem = BillingItemType.withName("power"),
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
   }
}
