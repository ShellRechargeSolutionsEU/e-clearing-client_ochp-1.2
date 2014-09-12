package com.thenewmotion.chargenetwork.eclearing

import java.util

import api._
import com.thenewmotion.chargenetwork.eclearing.api.BillingItem
import com.thenewmotion.chargenetwork.eclearing.api.CdrPeriod
import com.thenewmotion.chargenetwork.eclearing.api.ChargePointStatus.ChargePointStatus
import com.thenewmotion.chargenetwork.eclearing.api.ConnectorFormat
import com.thenewmotion.chargenetwork.eclearing.api.ConnectorStandard
import eu.ochp._1.{ConnectorType => GenConnectorType, EvseImageUrlType => GenEvseImageUrlType, EmtId => GenEmtId, CdrStatusType => GenCdrStatusType, ConnectorFormat => GenConnectorFormat, ConnectorStandard => GenConnectorStandard, CdrPeriodType => GenCdrPeriodType, BillingItemType => GenBillingItemType, _}
import org.joda.time.DateTime


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
      expiryDate = DateTimeNoMillis(rai.getExpiryDate.getDateTime)
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

  private def toOption (value: String):Option[String] = {
    value match {case null => None; case s if !s.isEmpty => Some(s); case _ => None}
  }

  private def toDateTimeOption (value: DateTimeType):Option[DateTime] = {
    value match {case null => None; case s if !s.getDateTime.isEmpty => Some(DateTimeNoMillis(s.getDateTime)); case _ => None}
  }

  private def toGeoPointOption (value: GeoPointType):Option[GeoPoint] = {
    value match {
      case null => None
      case gp: GeoPointType if (gp.getLat != null) && (gp.getLon != null) => Some(GeoPoint(gp.getLat, gp.getLon))
    }
  }

  private def toChargePointStatusOption(value: ChargePointStatusType): Option[ChargePointStatus] = {
    value match {
      case null => None
      case cps: ChargePointStatusType if !cps.getChargePointStatusType.isEmpty =>
        Some(ChargePointStatus.withName(cps.getChargePointStatusType))
      case _ => None
    }
  }

  private def toHoursOption (value: HoursType):Option[Hours] = {
    value match {
      case null => None;
      case hrs: HoursType => Some(Hours(
        regularHours = hrs.getRegularHours.asScala.toList map {rh =>
          RegularHours(
            rh.getWeekday,
            TimeNoSecs(rh.getPeriodBegin),
            TimeNoSecs(rh.getPeriodEnd))},
        exceptionalOpenings = hrs.getExceptionalOpenings.asScala.toList map {eo =>
          ExceptionalPeriod(
            periodBegin = DateTimeNoMillis(eo.getPeriodBegin.getDateTime),
            periodEnd = DateTimeNoMillis(eo.getPeriodEnd.getDateTime)
          )},
        exceptionalClosings = hrs.getExceptionalClosings.asScala.toList map {ec =>
          ExceptionalPeriod(
            periodBegin = DateTimeNoMillis(ec.getPeriodBegin.getDateTime),
            periodEnd = DateTimeNoMillis(ec.getPeriodEnd.getDateTime)
          )}))
    }
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
      startDateTime = DateTimeNoMillis(cdrinfo.getStartDateTime.getLocalDateTime),
      endDateTime = DateTimeNoMillis(cdrinfo.getEndDateTime.getLocalDateTime),
      duration = toOption(cdrinfo.getDuration),
      houseNumber = toOption(cdrinfo.getHouseNumber),
      address = toOption(cdrinfo.getAddress),
      zipCode = toOption(cdrinfo.getZipCode),
      city = toOption(cdrinfo.getCity),
      country = cdrinfo.getCountry,
      chargePointType = cdrinfo.getChargePointType,
      connectorType = Connector(
        connectorStandard = ConnectorStandard.withName(
          cdrinfo.getConnectorType.getConnectorStandard.getConnectorStandard),
        connectorFormat = ConnectorFormat.withName(
          cdrinfo.getConnectorType.getConnectorFormat.getConnectorFormat)),
      maxSocketPower = cdrinfo.getMaxSocketPower,
      productType = toOption(cdrinfo.getProductType),
      meterId = toOption(cdrinfo.getMeterId),
      chargingPeriods = cdrinfo.getChargingPeriods.asScala.toList.map( cdrPeriod=>
        CdrPeriod(
          startDateTime = DateTimeNoMillis(cdrPeriod.getStartDateTime.getLocalDateTime),
          endDateTime = DateTimeNoMillis(cdrPeriod.getEndDateTime.getLocalDateTime),
          billingItem = BillingItem.withName(cdrPeriod.getBillingItem.getBillingItemType) ,
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

  private def chargePeriodToGenCp(gcp: CdrPeriod): GenCdrPeriodType = {
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

  private def geoPointToGenGeoPoint(point: GeoPoint): GeoPointType = {
    val gpt = new GeoPointType()
    gpt.setLat(point.lat)
    gpt.setLon(point.lon)
    gpt
  }

  implicit def cpInfoToChargePoint(genCp: ChargePointInfo): ChargePoint = {
//    println("--\n" + genCp)
//    Thread.sleep(100)
//    println(genCp.getGeoLocation)
//    Thread.sleep(100)
//    println(genCp.getGeoLocation.getLat)
//    Thread.sleep(100)
    ChargePoint(
      evseId = genCp.getEvseId,
      locationId = genCp.getLocationId,
      timestamp = toDateTimeOption(genCp.getTimestamp),
      locationName = genCp.getLocationName,
      locationNameLang = genCp.getLocationNameLang,
      images = genCp.getImages.asScala.toList map {genImage => EvseImageUrl(
        uri = genImage.getUri,
        thumbUri = toOption(genImage.getThumbUri),
        clazz = genImage.getClazz,
        `type` = genImage.getType,
        width = Some(genImage.getWidth),
        height = Some(genImage.getHeight)
      )},
      address = CpAddress(
        houseNumber = toOption(genCp.getHouseNumber),
        address =  genCp.getAddress,
        city = genCp.getCity,
        zipCode = genCp.getZipCode,
        country = genCp.getCountry
      ),
      geoLocation = GeoPoint(
         lat = genCp.getGeoLocation match {case null => ""; case gl: GeoPointType => gl.getLat},
        lon = genCp.getGeoLocation match {case null => ""; case gl: GeoPointType => gl.getLon}),
      geoUserInterface = toGeoPointOption(genCp.getGeoUserInterface),
      geoSiteEntrance = genCp.getGeoSiteEntrance.asScala.toList map {gp =>
        GeoPoint(gp.getLat, gp.getLon)},
      geoSiteExit = genCp.getGeoSiteExit.asScala.toList map {gp =>
        GeoPoint(gp.getLat, gp.getLon)},
      operatingTimes = toHoursOption(genCp.getOperatingTimes),
      accessTimes = toHoursOption(genCp.getAccessTimes),
      status = toChargePointStatusOption(genCp.getStatus),
      statusSchedule = genCp.getStatusSchedule.asScala.toList map {cps =>
        ChargePointSchedule(DateTimeNoMillis(cps.getStartDate.getDateTime),
          DateTimeNoMillis(cps.getEndDate.getDateTime),
          ChargePointStatus.withName(cps.getStatus.getChargePointStatusType))},
      telephoneNumber = toOption(genCp.getTelephoneNumber),
      floorLevel = toOption(genCp.getFloorLevel),
      parkingSlotNumber = toOption(genCp.getParkingSlotNumber),
      parkingRestriction = genCp.getParkingRestriction.asScala.toList map {pr =>
        ParkingRestriction.withName(pr.getParkingRestrictionType)},
      authMethods = genCp.getAuthMethods.asScala.toList map {am =>
        AuthMethod.withName(am.getAuthMethodType)},
      connectors = genCp.getConnectors.asScala.toList map {con =>
        Connector(
        connectorStandard = ConnectorStandard.withName(
          con.getConnectorStandard.getConnectorStandard),
        connectorFormat = ConnectorFormat.withName(
          con.getConnectorFormat.getConnectorFormat))},
      userInterfaceLang = genCp.getUserInterfaceLang.asScala.toList
    )
  }

  private def imagesToGenImages(image: EvseImageUrl): GenEvseImageUrlType  = {
    val iut = new GenEvseImageUrlType()
    iut.setClazz(image.clazz)
    image.height map iut.setHeight
    image.width map iut.setWidth
    image.thumbUri map iut.setThumbUri
    iut.setType(image.`type`)
    iut.setUri(image.uri)
    iut
  }




  private def hoursOptionToHoursType(maybeHours: Option[Hours]): HoursType = {
    def regHoursToRegHoursType(regHours: RegularHours): RegularHoursType = {
      val regularHoursType = new RegularHoursType()
        regularHoursType.setWeekday(regHours.weekday)
        regularHoursType.setPeriodBegin(regHours.periodBegin.toString)
        regularHoursType.setPeriodEnd(regHours.periodEnd.toString)
      regularHoursType
    }
    def excPeriodToExcPeriodType(ep: ExceptionalPeriod): ExceptionalPeriodType = {
      val ept = new ExceptionalPeriodType()
      val begin = new DateTimeType
      begin.setDateTime(ep.periodBegin.toString)
      ept.setPeriodBegin(begin)
      val end = new DateTimeType
      end.setDateTime(ep.periodEnd.toString)
      ept.setPeriodEnd(end)
      ept
    }
    val hoursType = new HoursType()
    maybeHours map {hours =>
      hoursType.getRegularHours.addAll(hours.regularHours map regHoursToRegHoursType asJavaCollection)
      hoursType.getExceptionalOpenings.addAll(hours.exceptionalOpenings map excPeriodToExcPeriodType asJavaCollection)
      hoursType.getExceptionalClosings.addAll(hours.exceptionalClosings map excPeriodToExcPeriodType asJavaCollection)
    }
    hoursType
  }

  private def statSchedToGenStatSched(schedule: ChargePointSchedule): ChargePointScheduleType = {
    val cpst = new ChargePointScheduleType()
    val status = new ChargePointStatusType()
    status.setChargePointStatusType(schedule.status.toString)
    cpst.setStatus(status)
    val startDate = new DateTimeType()
    startDate.setDateTime(schedule.startDate.toString)
    cpst.setStartDate(startDate)
    val endDate = new DateTimeType()
    endDate.setDateTime(schedule.endDate.toString)
    cpst.setEndDate(endDate)
    cpst
  }

  private def parkRestrToGenParkRestr(pRestr: ParkingRestriction.Value): ParkingRestrictionType = {
    val prt = new ParkingRestrictionType()
    prt.setParkingRestrictionType(pRestr.toString)
    prt
  }

  private def authMethodToGenAuthMethod(authMethod: AuthMethod.Value): AuthMethodType = {
    val amt = new AuthMethodType()
    amt.setAuthMethodType(authMethod.toString)
    amt
  }
  
  private def connToGenConn(connector: Connector): GenConnectorType = {
    val ct = new GenConnectorType()
    val cs = new GenConnectorStandard()
    val cf = new GenConnectorFormat()
    cs.setConnectorStandard(connector.connectorStandard.toString)
    ct.setConnectorStandard(cs)
    cf.setConnectorFormat(connector.connectorFormat.toString)
    ct.setConnectorFormat(cf)
    ct
  }

  implicit def chargePointToGenCp(cp: ChargePoint): ChargePointInfo = {
    val cpi = new ChargePointInfo()
    cpi.setEvseId(cp.evseId)
    cpi.setLocationId(cp.locationId)
    cpi.timestamp map {t =>
      val ts = new DateTimeType()
      ts.setDateTime(t.toString)
      cpi.setTimestamp(ts)}
    cpi.setLocationName(cp.locationName)
    cpi.setLocationNameLang(cp.locationNameLang)
    cpi.getImages.addAll(cp.images.map {imagesToGenImages} asJavaCollection)
    cp.address.houseNumber map {hn => cpi.setAddress(hn)}
    cpi.setAddress(cp.address.address)
    cpi.setZipCode(cp.address.zipCode)
    cpi.setCity(cp.address.city)
    cpi.setCountry(cp.address.country)
    cpi.setGeoLocation(geoPointToGenGeoPoint(cp.geoLocation))
    cp.geoUserInterface map {gui => cpi.setGeoUserInterface(geoPointToGenGeoPoint(gui))}
    cpi.getGeoSiteEntrance.addAll(cp.geoSiteEntrance.map {geoPointToGenGeoPoint} asJavaCollection)
    cpi.getGeoSiteExit.addAll(cp.geoSiteExit.map {geoPointToGenGeoPoint} asJavaCollection)
    cpi.setOperatingTimes(hoursOptionToHoursType(cp.operatingTimes))
    cpi.setAccessTimes(hoursOptionToHoursType(cp.accessTimes))
    cp.status map {st =>
      val status = new ChargePointStatusType()
      status.setChargePointStatusType(st.toString)
      cpi.setStatus(status)}
    cpi.getStatusSchedule.addAll(cp.statusSchedule.map {statSchedToGenStatSched} asJavaCollection)
    cp.telephoneNumber map cpi.setTelephoneNumber
    cp.floorLevel map cpi.setFloorLevel
    cp.parkingSlotNumber map cpi.setParkingSlotNumber
    cpi.getParkingRestriction.addAll(cp.parkingRestriction.map {parkRestrToGenParkRestr} asJavaCollection)
    cpi.getAuthMethods.addAll(cp.authMethods.map {authMethodToGenAuthMethod} asJavaCollection)
    cpi.getConnectors.addAll(cp.connectors.map {connToGenConn} asJavaCollection)
    cpi.getUserInterfaceLang.addAll(cp.userInterfaceLang asJavaCollection)
    cpi
  }
}
