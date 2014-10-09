package com.thenewmotion.chargenetwork.eclearing.api

import com.thenewmotion.time.Imports._
import org.joda.time.format.ISODateTimeFormat

/**
 * Created with IntelliJ IDEA.
 * User: czwirello
 * Date: 11.09.14
 */
case class ChargePoint (
  evseId: String,
  locationId: String,
  timestamp: Option[DateTime] = None,
  locationName: String,
  locationNameLang: String,
  images: List[EvseImageUrl] = List(),
  address: CpAddress,
  geoLocation: GeoPoint,
  geoUserInterface: Option[GeoPoint] = None,
  geoSiteEntrance: List[GeoPoint] = List(),
  geoSiteExit: List[GeoPoint] = List(),
  operatingTimes: Option[Hours] = None,
  accessTimes: Option[Hours] = None,
  status: Option[ChargePointStatus.Value] = None,
  statusSchedule: List[ChargePointSchedule] = List(),
  telephoneNumber: Option[String] = None,
  floorLevel: Option[String] = None,
  parkingSlotNumber: Option[String] = None,
  parkingRestriction: List[ParkingRestriction.Value] = List(),
  authMethods: List[AuthMethod.Value], //must be non-empty
  connectors: List[Connector], //must be non-empty
  userInterfaceLang: List[String] = List()
) {
  val pat =
    """[A-Za-z]{2}\*[A-Za-z0-9]{3}\*[Ee][A-Za-z0-9][A-Za-z0-9\*]{0,30}|[A-Za-z]{2}
      |[A-Za-z0-9]{3}[Ee][A-Za-z0-9][A-Za-z0-9\*]{0,30}""".stripMargin
  require(evseId.matches(pat),
    s"evseId needs to conform to $pat")
}

case class CpAddress (
  houseNumber: Option[String] = None,
  address: String,
  city: String,
  zipCode: String,
  country: String
)

object CpTimestamp {
  val formatter = ISODateTimeFormat.dateTimeNoMillis()
  def apply(s: String) = formatter.parseDateTime(s)
  def unapply(dt: DateTime): String = dt.toString(formatter)
}

class GeoPoint(
   val lat: String,
   val lon: String
)

object GeoPoint {
  def apply(lat: String, lon: String) = {
    val ECLEARING_GEODATA_PRECISION = 6
    new GeoPoint(lat.substring(0, lat.indexOf(".") + 1 + ECLEARING_GEODATA_PRECISION),
      lon.substring(0, lon.indexOf(".") + 1 + ECLEARING_GEODATA_PRECISION))
  }
}

case class Hours (
  regularHours: List[RegularHours],
  exceptionalOpenings: List[ExceptionalPeriod],
  exceptionalClosings: List[ExceptionalPeriod]
)

case class RegularHours (
  weekday: Int = 0,
  periodBegin: TimeNoSecs,
  periodEnd: TimeNoSecs)

case class ExceptionalPeriod (
  periodBegin: DateTime,
  periodEnd: DateTime)

object ChargePointStatus extends QueryableEnumeration{
  type ChargePointStatus = Value
  val Unknown = Value("Unknown")
  val Operative = Value("Operative")
  val Inoperative = Value("Inoperative")
  val Planned = Value("Planned")
  val Closed = Value("Closed")
}


case class ChargePointSchedule (
  startDate: DateTime,
  endDate: DateTime,
  status: ChargePointStatus.Value)

object ParkingRestriction extends QueryableEnumeration{
  type parkingRestriction = Value
  val evonly = Value("evonly")
  val plugged = Value("plugged")
  val disabled = Value("disabled")
  val customers = Value("customers")
  val motorcycles = Value("motorcycles")}

object AuthMethod extends QueryableEnumeration {
  type AuthMethod = Value
  val Public = Value("Public")
  val LocalKey = Value("LocalKey")
  val DirectCash = Value("DirectCash")
  val DirectCreditcard = Value("DirectCreditcard")
  val DirectDebitcard = Value("DirectDebitcard")
  val RfidMifareCls = Value("RfidMifareCls")
  val RfidMifareDes = Value("RfidMifareDes")
  val RfidCallypso = Value("RfidCallypso")
  val Iec15118 = Value("Iec15118")
}

case class EvseImageUrl (
  uri: String,
  thumbUri: Option[String] = None,
  clazz: String,
  `type`: String,
  width: Option[Integer] = None,
  height: Option[Integer] = None
)