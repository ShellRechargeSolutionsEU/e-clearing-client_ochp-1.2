package com.thenewmotion.chargenetwork
package eclearing.api

import com.thenewmotion.chargenetwork.eclearing.api.TokenType.TokenType
import com.thenewmotion.chargenetwork.eclearing.client.Result
import com.thenewmotion.time.Imports._
import org.joda.time.format.ISODateTimeFormat

/**
 * @author Yaroslav Klymko
 */
trait EclearingApi {
  def recvAllCards(): List[Card]
  def sendAllCards(cards: List[Card]): Result[Card]
  def recvNewCards(lastUpdate: DateTime): List[Card]
  def sendNewCards(cards: List[Card]): Result[Card]

  def sendAllChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint]
  def recvAllChargePoints():List[ChargePoint]
  def sendNewChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint]
  def recvNewChargePoints(lastUpdate: DateTime):List[ChargePoint]

  def recvCdrs(): List[CDR]
  def sendCdrs(cards: List[CDR]): Result[CDR]

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

object ConnectorFormat extends Enumeration{
  type ConnectorFormat = Value
  val Socket = Value("Socket")
  val Cable = Value("Cable")
}

object ConnectorStandard extends Enumeration{
  type ConnectorStandard = Value
  val `CHADEMO IEC-62196-T1` = Value("CHADEMO IEC-62196-T1")
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
}

object CdrStatus extends Enumeration{
  type CdrStatus = Value
  val `new` = Value("new")
  val accepted = Value("accepted")
  val rejected = Value("rejected")
  val `owner declined` = Value("owner declined")
  val approved = Value("approved")
}

case class Card(contractId: String,
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

object TokenRepresentation extends Enumeration{
  type TokenRepresentation = Value
  val plain = Value("plain")
  val sha160 = Value("sha-160")
  val sha256 = Value("sha-256")
}

object TokenType extends Enumeration{
  type TokenType = Value
  val rfid = Value("rfid")
  val remote = Value("remote")
  val `15118` = Value("15118")
}

object TokenSubType {
  val Known = Seq(MifareCl, MifareDes, Msisdn, Other)
  def apply(id: Int): Value = Known.find(_.id == id) getOrElse Unknown(id)
  def withName(name: String): Value = Known.find(_.toString == name) getOrElse Other

  def forRfid(rfid: String): TokenSubType.Value = {
    require(rfid.matches("[0-9A-Fa-f]+"), "Rfid '%s' doesn't comply pattern '[0-9A-Fa-f]+'".format(rfid))
    rfid.size match {
      case 8 => MifareCl
      case 10 => MifareDes
      case _ => Other
    }
  }

  sealed abstract class Value {
    def id: Int
  }

  case object MifareCl extends Value {
    val id = 1
    override val toString = "mifareCls"
  }

  case object MifareDes extends Value {
    val id = 2
    override val toString = "mifareDes"
  }

  case object Msisdn extends Value {
    val id = 3
    override val toString = "msisdn"
  }

  case object Calypso extends Value {
    val id = 4
    override val toString = "calypso"
  }

  case object Other extends Value {
    val id = 0
    override val toString = "Other"
  }

  case class Unknown(id: Int) extends Value
}


case class EvseStatus(
  evseId: String,
  majorStatus: EvseStatusMajor.Value,
  minorStatus: Option[EvseStatusMinor.Value] = None
)

object EvseStatusMajor extends Enumeration {
  type EvseStatusMajor = Value
  val available = Value("available")
  val `not-available` = Value("not-available")
  val unknown = Value("unknown")
}


object EvseStatusMinor extends Enumeration {
  type EvseStatusMinor = Value
  val available = Value("available")
  val reserved = Value("reserved")
  val charging = Value("charging")
  val blocked = Value("blocked")
  val outoforder = Value("outoforder")
}