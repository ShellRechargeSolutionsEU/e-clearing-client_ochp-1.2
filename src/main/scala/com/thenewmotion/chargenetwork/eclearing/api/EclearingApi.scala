package com.thenewmotion.chargenetwork
package eclearing.api

import com.thenewmotion.chargenetwork.eclearing.api.TokenType.TokenType
import com.thenewmotion.chargenetwork.eclearing.client.Result
import com.thenewmotion.time.Imports._
import org.joda.time.format.ISODateTimeFormat
import scalax.StringOption

/**
 * @author Yaroslav Klymko
 */
trait EclearingApi {
  def recvCards(): Seq[Card]
  def sendCards(cards: Seq[Card]): Result
}


case class Card(evcoId: String,
                tokenType: TokenType = TokenType.Rfid,
                tokenSubType: TokenSubType.Value,
                tokenId: String,
                printedNumber: String,
                expiryDate: Option[DateTime])

object ExpiryDate {
  val formatter = ISODateTimeFormat.dateTimeNoMillis()

  def apply(s: String) = StringOption(s).map(formatter.parseDateTime)
  def unapply(dt: Option[DateTime]): String = dt.map(_.toString(formatter)).getOrElse("")
}

object TokenType extends Enumeration{
  type TokenType = Value
  val Rfid = Value("rfid")
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

