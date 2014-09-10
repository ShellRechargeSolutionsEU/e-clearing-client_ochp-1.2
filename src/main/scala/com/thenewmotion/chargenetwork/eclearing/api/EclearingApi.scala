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
  def recvCards(): List[Card]
  def sendCards(cards: List[Card]): Result

  def recvCdrs(): List[CDR]
  def sendCdrs(cards: List[CDR]): Result

}


case class CDR(
  cdrId: String,
  evseId: String,
  emtId: EmtId,
  contractId: String,
  liveAuthId: Option[String] = None,
  status: CdrStatusType.Value,
  startDateTime: DateTime,
  endDateTime: DateTime,
  duration: Option[String] = None,
  houseNumber: Option[String] = None,
  address: Option[String] = None,
  zipCode: Option[String] = None,
  city: Option[String] = None,
  country: String,
  chargePointType: String,
  connectorType: ConnectorType,
  maxSocketPower: Float,
  productType: Option[String] = None,
  meterId: Option[String] = None,
  chargingPeriods: List[CdrPeriodType])

object CdrStartDateTime {
  val formatter = ISODateTimeFormat.dateTimeNoMillis()
  def apply(s: String) = formatter.parseDateTime(s)
  def unapply(dt: DateTime): String = dt.toString(formatter)
}
object CdrEndDateTime {
  val formatter = ISODateTimeFormat.dateTimeNoMillis()
  def apply(s: String) = formatter.parseDateTime(s)
  def unapply(dt: DateTime): String = dt.toString(formatter)
}

case class CdrPeriodType (
  startDateTime: DateTime,
  endDateTime: DateTime,
  billingItem: BillingItemType.Value,
  billingValue: Float,
  currency: String,
  itemPrice: Float,
  periodCost: Option[Float]
)

object BillingItemType extends Enumeration{
  type BillingItemType = Value
  val parkingtime = Value("parkingtime")
  val usagetime = Value("usagetime")
  val energy = Value("energy")
  val power = Value("power")
  val serviceFee = Value("serviceFee")

}

case class EmtId(
  tokenType: TokenType = TokenType.rfid,
  tokenSubType: Option[TokenSubType.Value] = None,
  tokenId: String
)

case class ConnectorType (
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

object CdrStatusType extends Enumeration{
  type CdrStatusType = Value
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

object ExpiryDate {
  val formatter = ISODateTimeFormat.dateTimeNoMillis()
  def apply(s: String) = formatter.parseDateTime(s)
  def unapply(dt: DateTime): String = dt.toString(formatter)
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

  case object Other extends Value {
    val id = 0
    override val toString = "Other"
  }

  case class Unknown(id: Int) extends Value
}

