package com.thenewmotion.chargenetwork.eclearing.api

import com.thenewmotion.time.Imports._
import org.joda.time.format.ISODateTimeFormat

/**
 * Created with IntelliJ IDEA.
 * User: czwirello
 * Date: 11.09.14
 */
case class ChargePoint (
  evseId: EvseId,
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
)

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

case class GeoPoint(lat: Double, lon: Double)

object GeoPoint {
  def fmt(x: Double) = "%3.6f".formatLocal(java.util.Locale.US, x)
  def apply(lat: String, lon: String) = new GeoPoint(lat.toDouble, lon.toDouble)
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

object ImageClass extends QueryableEnumeration {
  type ImageClass = Value
  val networkLogo = Value("networkLogo")
  val operatorLogo = Value("operatorLogo")
  val ownerLogo = Value("ownerLogo")
  val stationPhoto = Value("stationPhoto")
  val locationPhoto = Value("locationPhoto")
  val entrancePhoto = Value("entrancePhoto")
  val otherPhoto = Value("otherPhoto")
  val otherLogo = Value("otherLogo")
  val otherGraphic = Value("otherGraphic")
}

case class EvseImageUrl (
  uri: String,
  thumbUri: Option[String] = None,
  clazz: ImageClass.Value,
  `type`: String,
  width: Option[Integer] = None,
  height: Option[Integer] = None
)

case class EvseId(value: String) {
  require(value.matches(EvseId.pattern), s"evseId needs to conform to ${EvseId.pattern} but was $value")
}

object EvseId {
  val pattern = """([A-Za-z]{2})\*?([A-Za-z0-9]{3})\*?[Ee][A-Za-z0-9][A-Za-z0-9\*]{0,30}"""
  val regex = pattern.r
}

case class EvseOperator(name: String, country: String)

object EvseOperator {

  private val evseOperatorNameMap = Map(
    (("IE", "845") -> "ESBI Engineering and Facility Management Limited"),
    (("BE", "846") -> "BlueCorner NV"),
    (("AT", "847") -> "Vorarlberger Kraftwerke AG"),
    (("AT", "850") -> "Wien Energie GmbH"),
    (("AT", "852") -> "Salzburg AG für Energie, Verkehr und Telekommunikation"),
    (("DE", "853") -> "Ahrtal-Werke GmbH"),
    (("AT", "854") -> "KELAG-Kärntner Elektrizitäts-AG"),
    (("NL", "857") -> "Essent New Energy BV"),
    (("NL", "ALL") -> "Allego"),
    (("NL", "ANW") -> "ANWB"),
    (("NL", "BAL") -> "Ballast Nedam"),
    (("NL", "BCU") -> "Blue Current"),
    (("NL", "EVN") -> "EVnetNL"),
    (("NL", "ENE") -> "Eneco"),
    (("NL", "ESS") -> "Essent"),
    (("NL", "EVB") -> "EV-Box"),
    (("NL", "GFX") -> "Green Flux"),
    (("NL", "LMS") -> "LastMileSolutions"),
    (("NL", "NUO") -> "Nuon"),
    (("NL", "TNM") -> "The New Motion"),
    (("ES", "838") -> "IBERDROLA GENERACIÓN S.A.U."),
    (("DE", "810") -> "EWE AG"),
    (("DE", "811") -> "Linuxpartner GmbH"),
    (("DE", "812") -> "MVV Energie AG"),
    (("DE", "815") -> "Stadtwerke Springe GmbH"),
    (("DE", "817") -> "Siemens AG"),
    (("DE", "820") -> "Stadtwerke Waldkirch GmbH"),
    (("DE", "821") -> "EnBW Energie Baden-Württemberg AG"),
    (("DE", "822") -> "Belectric Drive GmbH"),
    (("DE", "826") -> "Stadtwerke Schwäbisch Hall GmbH"),
    (("DE", "841") -> "AUDI AG"),
    (("DE", "843") -> "Bosch Software Innovations GmbH"),
    (("DE", "855") -> "Energieversorgung Sehnde GmbH"),
    (("DE", "859") -> "Plugsurfing GmbH"),
    (("DE", "860") -> "E.ON Technologies GmbH"),
    (("DE", "123") -> "Move About GmbH"),
    (("DE", "IAV") -> "IAV GmbH Ingenieurgesellschaft Auto und Verkehr"),
    (("DE", "NAG") -> "NATURSTROM AG"),
    (("DE", "TNM") -> "The New Motion Deutschland GmbH"),
    (("DE", "861") -> "Stadtwerke Leipzig GmbH"),
    (("DE", "EBG") -> "EBG Compleo GmbH"),
    (("DE", "STA") -> "Stadtwerke Aachen AG"),
    (("DE", "LND") -> "smartlab Innovationsgesellschaft mbH"),
    (("DE", "SWW") -> "Stadtwerke Weimar Stadtversorgungs-GmbH"),
    (("DE", "ENL") -> "Enovos Luxembourg"),
    (("DE", "SWE") -> "SWE Energie GmbH"),
    (("DE", "SWD") -> "Stadtwerke Düsseldorf AG"),
    (("DE", "VAT") -> "Vattenfall Europe Innovation GmbH"),
    (("DE", "REK") -> "RheinEnergie AG"),
    (("DE", "UBI") -> "ubitricity Gesellschaft für verteilte Energiesysteme mbH"),
    (("DE", "SLB") -> "Stadtwerke Ludwigsburg-Kornwestheim GmbH"),
    (("DE", "NDS") -> "T-Systems International GmbH"),
    (("DE", "247") -> "Chargepartner GmbH"),
    (("DE", "SWM") -> "Stadtwerke München GmbH"),
    (("DE", "359") -> "swb Vertrieb Bremen GmbH"),
    (("DE", "WSW") -> "WSW Energie und Wasser AG"),
    (("DE", "SBR") -> "Stadtwerke Brühl GmbH"),
    (("DE", "GMH") -> "Stadtwerke Georgsmarienhütte GmbH"),
    (("DE", "SWS") -> "Stadtwerke Bad Säckingen GmbH"),
    (("DE", "SWT") -> "SWT-AöR"),
    (("DE", "SUN") -> "SUN GmbH & Co. KG"),
    (("DE", "EVS") -> "Energieversorgung Sylt GmbH"),
    (("DE", "AUW") -> "Allgäuer Überlandwerk GmbH"),
    (("DE", "DVV") -> "Stadtwerke Duisburg AG"),
    (("DE", "EBE") -> "Ebee Smart Technologies GmbH"),
    (("DE", "UEZ") -> "Unterfränkische Überlandzentrale eG"),
    (("DE", "SWR") -> "StadtWerke Rösrath - Energie GmbH"),
    (("DE", "MEK") -> "MENNEKES Elektrotechnik GmbH & Co. KG"),
    (("DE", "ALL") -> "Allego GmbH"),
    (("DE", "STW") -> "Stadtwerke Waldshut-Tiengen GmbH"),
    (("DE", "RWE") -> "RWE Effizienz GmbH"),
    (("DE", "SGD") -> "Stadtwerke Schwäbisch Gmünd GmbH"),
    (("DE", "SWH") -> "Stadtwerke EVB Huntetal GmbH"),
    (("DE", "ENW") -> "enwor - energie & wasser vor ort GmbH"),
    (("DE", "JEN") -> "Stadtwerke Energie Jena-Pößneck GmbH"),
    (("DE", "DAI") -> "Technische Universität Berlin"),
    (("DE", "TEN") -> "Teutoburger Energie Netzwerk eG (TEN eG)"),
    (("DE", "111") -> "NürnbergMesse GmbH"),
    (("DE", "SWO") -> "Stadtwerke Osnabrück AG"),
    (("DE", "SWN") -> "Stadtwerke Nürtingen GmbH"),
    (("DE", "MET") -> "Stadtwerke Metzingen"),
    (("DE", "1HD") -> "Stadtwerke Heidelberg Energie GmbH"),
    (("DE", "MEN") -> "Stadtwerke Menden GmbH"),
    (("DE", "EVB") -> "Eisenacher Versorgungs-Betriebe GmbH"),
    (("DE", "EWM") -> "Elektrizitätswerk Mittelbaden AG & Co. KG"),
    (("DE", "WEM") -> "Stadtwerke Weilheim Energie GmbH"),
    (("DE", "SVB") -> "Siegener Versorgungsbetriebe GmbH"),
    (("DE", "ROS") -> "Stadtwerke Rosenheim GmbH & Co. KG"),
    (("DE", "CIT") -> "CIRRANTIC GmbH"),
    (("DE", "SWJ") -> "Stadtwerke Jülich GmbH"),
    (("DE", "333") -> "Stadtwerke Ingolstadt Beteiligungen GmbH"),
    (("DE", "NCE") -> "Nissan Center Europe GmbH"),
    (("DE", "VWX") -> "Volkswagen Aktiengesellschaft"),
    (("DE", "SNH") -> "Stromnetz Hamburg GmbH"),
    (("DE", "730") -> "Heldele GmbH"),
    (("DE", "EN0") -> "enewa GmbH"),
    (("DE", "666") -> "E-WALD GmbH"),
    (("DE", "EGH") -> "Elektrizitätsgenossenschaft e.G. Hasbergen"),
    (("DE", "FFB") -> "Stadtwerke Fürstenfeldbruck GmbH"),
    (("DE", "BMW") -> "Bayerische Motoren Werke AG"),
    (("DE", "731") -> "Energieversorgung Filstal GmbH & Co. KG"),
    (("DE", "SEE") -> "STADTWERK AM SEE GmbH & Co. KG"),
    (("DE", "SKB") -> "Städtisches Kommunalunternehmen Baiersdorf, Anstalt des öffentlichen Rechts der Stadt Baiersdorf"),
    (("DE", "SMA") -> "E-Mobility Provider Austria GmbH & CO KG"),
    (("DE", "DBE") -> "DB Energie GmbH"),
    (("DE", "TWS") -> "Technische Werke Schussental GmbH & Co. KG"),
    (("DE", "WES") -> "Stadtwerke Wesel GmbH"),
    (("DE", "SSW") -> "Schleswiger Stadtwerke GmbH"),
    (("DE", "EEM") -> "eeMobility GmbH"),
    (("BE", "EST") -> "Estonteco S.a.r.l.")
  )

  def apply(id: EvseId): Option[EvseOperator] = id.value match {
    case EvseId.regex(country, evseOperatorId) => evseOperatorNameMap.get(country -> evseOperatorId).map(name =>
      EvseOperator(name, country)
    )
    case _ => None
  }

}
