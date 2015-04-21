package com.thenewmotion.ochp
package api

import TokenType.TokenType
import com.thenewmotion.ochp.client.Result
import com.thenewmotion.time.Imports._
import org.joda.time.format.ISODateTimeFormat

trait OchpApi {
  def recvAllTokens(): List[ChargeToken]
  def sendAllTokens(tokens: List[ChargeToken]): Result[ChargeToken]
  def recvNewTokens(lastUpdate: DateTime): List[ChargeToken]
  def sendNewTokens(tokens: List[ChargeToken]): Result[ChargeToken]

  def sendAllChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint]
  def recvAllChargePoints():Result[ChargePoint]
  def sendNewChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint]
  def recvNewChargePoints(lastUpdate: DateTime):Result[ChargePoint]

  def sendCdrs(cdrs: List[CDR]): Result[CDR]
  def recvCdrs(): List[CDR]
  def confCdrs(approvedCdrs: List[CDR], declinedCdrs: List[CDR])
}



case class EmtId(
  tokenType: TokenType = TokenType.rfid,
  tokenSubType: Option[TokenSubType.Value] = None,
  tokenId: String
)

case class Connector (
  connectorStandard: ConnectorStandard.Value,
  connectorFormat: ConnectorFormat.Value
)

object ConnectorFormat extends QueryableEnumeration{
  type ConnectorFormat = Value
  val Socket = Value("Socket")
  val Cable = Value("Cable")
}

object ConnectorStandard extends QueryableEnumeration{
  type ConnectorStandard = Value
  val `CHADEMO` = Value("Chademo")
  val `IEC-62196-T1` = Value("IEC-62196-T1")
  val `IEC-62196-T1-COMBO` = Value("IEC-62196-T1-COMBO")
  val `IEC-62196-T2` = Value("IEC-62196-T2")
  val `IEC-62196-T2-COMBO` = Value("IEC-62196-T2-COMBO")
  val `IEC-62196-T3A` = Value("IEC-62196-T3A")
  val `IEC-62196-T3C` = Value("IEC-62196-T3C")
  val `DOMESTIC-A` = Value("DOMESTIC-A")
  val `DOMESTIC-B` = Value("DOMESTIC-B")
  val `DOMESTIC-C` = Value("DOMESTIC-C")
  val `DOMESTIC-D` = Value("DOMESTIC-D")
  val `DOMESTIC-E` = Value("DOMESTIC-E")
  val `DOMESTIC-F` = Value("DOMESTIC-F")
  val `DOMESTIC-G` = Value("DOMESTIC-G")
  val `DOMESTIC-H` = Value("DOMESTIC-H")
  val `DOMESTIC-I` = Value("DOMESTIC-I")
  val `DOMESTIC-J` = Value("DOMESTIC-J")
  val `DOMESTIC-K` = Value("DOMESTIC-K")
  val `DOMESTIC-L` = Value("DOMESTIC-L")
  val `TESLA-R` = Value("TESLA-R")
  val `TESLA-S` = Value("TESLA-S")
  val `IEC-60309-2-single-16` = Value("IEC-60309-2-single-16")
  val `IEC-60309-2-three-16` = Value("IEC-60309-2-three-16")
  val `IEC-60309-2-three-32` = Value("IEC-60309-2-three-32")
  val `IEC-60309-2-three-64` = Value("IEC-60309-2-three-64")
}

object CdrStatus extends QueryableEnumeration{
  type CdrStatus = Value
  val `new` = Value("new")
  val accepted = Value("accepted")
  val rejected = Value("rejected")
  val `owner declined` = Value("owner declined")
  val approved = Value("approved")
}

case class ChargeToken(contractId: String,
                emtId: EmtId,
                printedNumber: Option[String],
                expiryDate: DateTime)

case class DateTimeNoMillis(
  dateTime: DateTime
){
  override def toString = DateTimeNoMillis.formatter.print(dateTime)
}

object DateTimeNoMillis {
  val formatter = ISODateTimeFormat.dateTimeNoMillis()
  def apply(s: String) = formatter.parseDateTime(s)
}

case class TimeNoSecs(
  time: LocalTime
){
  override def toString = TimeNoSecs.formatter.print(time)
}

object TimeNoSecs {
  val formatter = ISODateTimeFormat.hourMinute()
  def apply(s: String): TimeNoSecs = TimeNoSecs(formatter.parseLocalTime(s))
}

object TokenRepresentation extends QueryableEnumeration{
  type TokenRepresentation = Value
  val plain = Value("plain")
  val sha160 = Value("sha-160")
  val sha256 = Value("sha-256")
}

object TokenType extends QueryableEnumeration{
  type TokenType = Value
  val rfid = Value("rfid")
  val remote = Value("remote")
  val `15118` = Value("15118")
}

object TokenSubType extends QueryableEnumeration{
  type TokenSubType = Value
  val mifareCls = Value("mifareCls")
  val mifareDes = Value("mifareDes")
  val calypso = Value("calypso")

  def forRfid(rfid: String): Option[TokenSubType.Value] = {
       require(rfid.matches("[0-9A-Fa-f]+"), "Rfid '%s' doesn't comply pattern '[0-9A-Fa-f]+'".format(rfid))
       rfid.length match {
         case 8 => Some(mifareCls)
         case 10 => Some(mifareDes)
         case _ => None
       }
  }
}

class QueryableEnumeration extends Enumeration {
  def exists(name: String) = values.exists(_.toString == name)
  def withNameOpt(name: String) = if (exists(name)) Some(this.withName(name)) else None
}


case class EvseStatus(
  evseId: EvseId,
  majorStatus: EvseStatusMajor.Value,
  minorStatus: Option[EvseStatusMinor.Value] = None
)

object EvseStatusMajor extends QueryableEnumeration {
  type EvseStatusMajor = Value
  val available = Value("available")
  val `not-available` = Value("not-available")
  val unknown = Value("unknown")
}


object EvseStatusMinor extends QueryableEnumeration {
  type EvseStatusMinor = Value
  val available = Value("available")
  val reserved = Value("reserved")
  val charging = Value("charging")
  val blocked = Value("blocked")
  val outoforder = Value("outoforder")
}
