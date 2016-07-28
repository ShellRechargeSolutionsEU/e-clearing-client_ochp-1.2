package com.thenewmotion.ochp

import api._
import com.thenewmotion.time.Imports._
import ChargePointStatus.ChargePointStatus
import eu.ochp._1.{ConnectorType => GenConnectorType, EvseImageUrlType => GenEvseImageUrlType, EmtId => GenEmtId, CdrStatusType => GenCdrStatusType, ConnectorFormat => GenConnectorFormat, ConnectorStandard => GenConnectorStandard, CdrPeriodType => GenCdrPeriodType, BillingItemType => GenBillingItemType, EvseStatusType => GetEvseStatusType, _}
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import scala.util.{Try, Success, Failure}
import scala.language.{implicitConversions, postfixOps}

/**
 *
 * Convert between cxf-generated java classes and nice scala case classes
 *
 */
object Converters {
  import scala.collection.JavaConverters._

  private val logger = LoggerFactory.getLogger(Converters.getClass)

  implicit def roamingAuthorisationInfoToToken(rai: RoamingAuthorisationInfo): ChargeToken = {
    ChargeToken(
      contractId = rai.getContractId,
      emtId = EmtId(
        tokenId = rai.getEmtId.getInstance,
        tokenType = TokenType.withName(rai.getEmtId.getTokenType),
        tokenSubType = Option(rai.getEmtId.getTokenSubType) map {TokenSubType.withName}),
      printedNumber = Option(rai.getPrintedNumber),
      expiryDate = DateTimeNoMillis(rai.getExpiryDate.getDateTime)
    )
  }

  implicit def tokenToRoamingAuthorisationInfo(token: ChargeToken): RoamingAuthorisationInfo = {
    import token._

    val rai = new RoamingAuthorisationInfo()
    val emtId = new GenEmtId()
    rai.setContractId(contractId)
    emtId.setInstance(token.emtId.tokenId)
    emtId.setTokenType(token.emtId.tokenType.toString)
    token.emtId.tokenSubType foreach {st => emtId.setTokenSubType(st.toString)}
    emtId.setRepresentation("plain")
    rai.setEmtId(emtId)
    token.printedNumber foreach {pn => rai.setPrintedNumber(pn.toString)}
    rai.setExpiryDate(toDateTimeType(expiryDate))
    rai
  }

  private def toOption (value: String):Option[String] =
    Option(value).find(_.nonEmpty)

  private def toDateTimeOption (value: DateTimeType):Option[DateTime] =
    Option(value).flatMap{v => Try(DateTimeNoMillis(v.getDateTime)) match {
      case Success(x) => Some(x)
      case Failure(e) => logger.error("Date and time value parsing failure", e); None
    }}

  private def toRegularHours (rh: RegularHoursType): Option[RegularHours] = {

    val normalize: PartialFunction[String, String] = {
      case "24:00" => "23:59"
      case x => x
    }

    def toTime(t: String): Option[TimeNoSecs] =
      Option(t).flatMap{v => Try(TimeNoSecs(normalize(v))) match {
        case Success(x) => Some(x)
        case Failure(e) => logger.error("Time value parsing failure", e); None
      }}

    for {
      beg <- toTime(rh.getPeriodBegin)
      end <- toTime(rh.getPeriodEnd)
    } yield RegularHours(rh.getWeekday, beg, end)
  }

  private def toChargePointStatusOption(value: ChargePointStatusType): Option[ChargePointStatus] =
    Option(value).flatMap(v => Try(ChargePointStatus.withName(v.getChargePointStatusType)) match {
      case Success(x) => Some(x)
      case Failure(e) => logger.error("Charge point status parsing failure", e); None
    })

  private[ochp] def validate(value: HoursType) = {
    def prettyPrint(ht: HoursType) =
      s"""HoursType(
        |regularHours = ${ht.getRegularHours},
        | twentyfourseven = ${ht.isTwentyfourseven},
        | exceptionalOpenings = ${ht.getExceptionalOpenings},
        | exceptionalClosings = ${ht.getExceptionalClosings})
      """.stripMargin.replaceAll("\n", "")

    val invalid =
      Option(value.isTwentyfourseven).fold(true) { _ == false } &&
        value.getRegularHours.isEmpty &&
        value.getExceptionalOpenings.isEmpty &&
        value.getExceptionalClosings.isEmpty

    if(invalid)
      throw new IllegalArgumentException(
        s"Provided hoursType ${prettyPrint(value)} cannot be accepted because it does not define 24/7 nor any hours")

    true
  }

  private[ochp] def toHoursOption(value: HoursType): Option[Hours] = {
    def regularHours(v: HoursType): Either[List[RegularHours], Boolean] =
      Option(v.isTwentyfourseven)
        .map(tfs => Right(tfs == true))
        .getOrElse(Left(v.getRegularHours.asScala.toList.flatMap(toRegularHours)))

    Option(value)
      .map { v =>

        validate(v)

        Hours(
          regularHoursOrTwentyFourSeven = regularHours(v),
          exceptionalOpenings = v.getExceptionalOpenings.asScala.toList flatMap {eo =>
            for {
              beg <- toDateTimeOption(eo.getPeriodBegin)
              end <- toDateTimeOption(eo.getPeriodEnd)
            } yield ExceptionalPeriod(beg, end)
          },
          exceptionalClosings = v.getExceptionalClosings.asScala.toList flatMap {ec =>
            for {
              beg <- toDateTimeOption(ec.getPeriodBegin)
              end <- toDateTimeOption(ec.getPeriodEnd)
            } yield ExceptionalPeriod(beg, end)
          }
        )
    }
  }

  implicit def cdrInfoToCdr(cdrinfo: CDRInfo): CDR =
    CDR(
      cdrId = cdrinfo.getCdrId,
      evseId = cdrinfo.getEvseId,
      emtId = EmtId(
        tokenId = cdrinfo.getEmtId.getInstance,
        tokenType = TokenType.withName(cdrinfo.getEmtId.getTokenType),
        tokenSubType = Option(cdrinfo.getEmtId.getTokenSubType) map {TokenSubType.withName}
      ),
      contractId = cdrinfo.getContractId,
      liveAuthId = toOption(cdrinfo.getLiveAuthId),
      status = CdrStatus.withName(cdrinfo.getStatus.getCdrStatusType),
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
      chargingPeriods = cdrinfo.getChargingPeriods.asScala.toList.map( cdrPeriod=> {
        val cost = cdrPeriod.getPeriodCost
        CdrPeriod(
          startDateTime = DateTimeNoMillis(cdrPeriod.getStartDateTime.getLocalDateTime),
          endDateTime = DateTimeNoMillis(cdrPeriod.getEndDateTime.getLocalDateTime),
          billingItem = BillingItem.withName(cdrPeriod.getBillingItem.getBillingItemType),
          billingValue = cdrPeriod.getBillingValue,
          currency = cdrPeriod.getCurrency,
          itemPrice = cdrPeriod.getItemPrice,
          periodCost = Option(cost.toFloat)
        )
      })
    )

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
    cdr.emtId.tokenSubType.map(tst => eid.setTokenSubType(tst.toString))
    cdrInfo.setEmtId(eid)
    val start = new LocalDateTimeType()
    start.setLocalDateTime(DateTimeNoMillis(startDateTime).toString)
    cdrInfo.setStartDateTime(start)
    val end = new LocalDateTimeType()
    end.setLocalDateTime(DateTimeNoMillis(endDateTime).toString)
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
    start.setLocalDateTime(DateTimeNoMillis(gcp.startDateTime).toString)
    period1.setStartDateTime(start)
    val end = new LocalDateTimeType()
    end.setLocalDateTime(DateTimeNoMillis(gcp.endDateTime).toString)
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

  private def toGeoPoint(value: GeoPointType) =
    GeoPoint(value.getLat, value.getLon)

  private def toGeoPointType(point: GeoPoint): GeoPointType = {
    import GeoPoint.fmt

    val gpt = new GeoPointType()
    gpt.setLat(fmt(point.lat))
    gpt.setLon(fmt(point.lon))
    gpt
  }

  private def toAdditionalGeoPoint(value: AdditionalGeoPointType) =
    AdditionalGeoPoint(
      GeoPoint(value.getLat, value.getLon),
      Option(value.getName),
      GeoPointTypeEnum.withName(value.getType))

  private def toAdditionalGeoPointType(value: AdditionalGeoPoint) = {
    import GeoPoint.fmt

    val res = new AdditionalGeoPointType()
    res.setLat(fmt(value.point.lat))
    res.setLon(fmt(value.point.lon))
    value.name.foreach(res.setName)
    res.setType(value.typ.toString)
    res
  }

  private def getTimeZone(tz: String): Option[DateTimeZone] =
    toOption(tz).flatMap(z => Try(DateTimeZone.forID(z)).toOption)

  implicit def cpInfoToChargePoint(genCp: ChargePointInfo): Option[ChargePoint] = Try{
    ChargePoint(
      evseId = EvseId(genCp.getEvseId),
      locationId = genCp.getLocationId,
      timestamp = toDateTimeOption(genCp.getTimestamp),
      locationName = genCp.getLocationName,
      locationNameLang = genCp.getLocationNameLang,
      images = genCp.getImages.asScala.toList map {genImage => EvseImageUrl(
        uri = genImage.getUri,
        thumbUri = toOption(genImage.getThumbUri),
        clazz = ImageClass.withName(genImage.getClazz),
        `type` = genImage.getType,
        width = Option(genImage.getWidth),
        height = Option(genImage.getHeight)
      )},
      relatedResources = genCp.getRelatedResource.asScala.toList.map { res =>
        RelatedResource(res.getUri, RelatedResourceTypeEnum.withName(res.getClazz))
      },
      address = CpAddress(
        houseNumber = toOption(genCp.getHouseNumber),
        address =  genCp.getAddress,
        city = genCp.getCity,
        zipCode = genCp.getZipCode,
        country = genCp.getCountry
      ),
      chargePointLocation = toGeoPoint(genCp.getChargePointLocation),
      relatedLocations = genCp.getRelatedLocation.asScala.toList.map(toAdditionalGeoPoint),
      timeZone = getTimeZone(genCp.getTimeZone),
      category = toOption(genCp.getCategory),
      operatingTimes = toHoursOption(genCp.getOperatingTimes),
      accessTimes = toHoursOption(genCp.getAccessTimes),
      status = toChargePointStatusOption(genCp.getStatus),
      statusSchedule = genCp.getStatusSchedule.asScala.toList flatMap {cps => Try{
        for {
          beg <- toDateTimeOption(cps.getStartDate)
          end <- toDateTimeOption(cps.getEndDate)
        } yield ChargePointSchedule(beg, end,
          ChargePointStatus.withName(cps.getStatus.getChargePointStatusType))
      } match {
        case Success(x) => x
        case Failure(e) => logger.error("Status schedule parsing failure", e); None
      }},
      telephoneNumber = toOption(genCp.getTelephoneNumber),
      location = GeneralLocation.withName(genCp.getLocation.getGeneralLocationType),
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
      ratings = toRatingsOption(genCp.getRatings),
      userInterfaceLang = genCp.getUserInterfaceLang.asScala.toList
    )
  } match {
    case Success(x) => Some(x)
    case Failure(e) => logger.error("Charge point conversion failure", e); None
  }

  private def imagesToGenImages(image: EvseImageUrl): GenEvseImageUrlType  = {
    val iut = new GenEvseImageUrlType()
    iut.setClazz(image.clazz.toString)
    image.height foreach iut.setHeight
    image.width foreach iut.setWidth
    image.thumbUri foreach iut.setThumbUri
    iut.setType(image.`type`)
    iut.setUri(image.uri)
    iut
  }

  private[ochp] def hoursOptionToHoursType(maybeHours: Option[Hours]): HoursType = {
    def regHoursToRegHoursType(regHours: RegularHours): RegularHoursType = {
      val regularHoursType = new RegularHoursType()
      regularHoursType.setWeekday(regHours.weekday)
      regularHoursType.setPeriodBegin(regHours.periodBegin.toString)
      regularHoursType.setPeriodEnd(regHours.periodEnd.toString)
      regularHoursType
    }

    def excPeriodToExcPeriodType(ep: ExceptionalPeriod): ExceptionalPeriodType = {
      val ept = new ExceptionalPeriodType()
      ept.setPeriodBegin(toDateTimeType(ep.periodBegin))
      ept.setPeriodEnd(toDateTimeType(ep.periodEnd))
      ept
    }

    maybeHours.map { hours =>
      val hoursType = new HoursType()

      hours.regularHoursOrTwentyFourSeven.fold(
        regHours =>
          hoursType.getRegularHours.addAll(
            regHours.map(regHoursToRegHoursType).asJavaCollection),
        twentyFourSeven =>
          hoursType.setTwentyfourseven(twentyFourSeven))
      hoursType.getExceptionalOpenings.addAll(
        hours.exceptionalOpenings map excPeriodToExcPeriodType asJavaCollection)
      hoursType.getExceptionalClosings.addAll(
        hours.exceptionalClosings map excPeriodToExcPeriodType asJavaCollection)

      hoursType
    }.getOrElse(null)
  }

  private def statSchedToGenStatSched(schedule: ChargePointSchedule): ChargePointScheduleType = {
    val cpst = new ChargePointScheduleType()
    val status = new ChargePointStatusType()
    status.setChargePointStatusType(schedule.status.toString)
    cpst.setStatus(status)
    cpst.setStartDate(toDateTimeType(schedule.startDate))
    cpst.setEndDate(toDateTimeType(schedule.endDate))
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

  private def toGeneralLocationType(gl: GeneralLocation.Value): GeneralLocationType = {
    val glt = new GeneralLocationType()
    glt.setGeneralLocationType(gl.toString)
    glt
  }

  private def toRelatedResourceType(res: RelatedResource) = {
    val resource = new RelatedResourceType()
    resource.setUri(res.uri)
    resource.setClazz(res.`class`.toString)
    resource
  }

  implicit def chargePointToCpInfo(cp: ChargePoint): ChargePointInfo = {
    val cpi = new ChargePointInfo()
    cpi.setEvseId(cp.evseId.value)
    cpi.setLocationId(cp.locationId)
    cp.timestamp foreach {t =>
      cpi.setTimestamp(toDateTimeType(t))}
    cpi.setLocationName(cp.locationName)
    cpi.setLocationNameLang(cp.locationNameLang)
    cpi.getImages.addAll(cp.images.map {imagesToGenImages} asJavaCollection)
    cpi.getRelatedResource.addAll(
      cp.relatedResources.map(toRelatedResourceType).asJavaCollection)
    cp.address.houseNumber foreach {hn => cpi.setAddress(hn)}
    cpi.setAddress(cp.address.address)
    cpi.setZipCode(cp.address.zipCode)
    cpi.setCity(cp.address.city)
    cpi.setCountry(cp.address.country)
    cpi.setChargePointLocation(toGeoPointType(cp.chargePointLocation))
    cpi.getRelatedLocation.addAll(
      cp.relatedLocations.map(toAdditionalGeoPointType).asJavaCollection)
    cp.timeZone.map(tz => cpi.setTimeZone(tz.toString))
    cp.category.map(cpi.setCategory)
    cpi.setOperatingTimes(hoursOptionToHoursType(cp.operatingTimes))
    cpi.setAccessTimes(hoursOptionToHoursType(cp.accessTimes))
    cp.status.foreach { st =>
      val status = new ChargePointStatusType()
      status.setChargePointStatusType(st.toString)
      cpi.setStatus(status)
    }
    cpi.getStatusSchedule.addAll(cp.statusSchedule.map {statSchedToGenStatSched} asJavaCollection)
    cp.telephoneNumber foreach cpi.setTelephoneNumber
    cpi.setLocation(toGeneralLocationType(cp.location))
    cp.floorLevel foreach cpi.setFloorLevel
    cp.parkingSlotNumber foreach cpi.setParkingSlotNumber
    cpi.getParkingRestriction.addAll(cp.parkingRestriction.map {parkRestrToGenParkRestr} asJavaCollection)
    cpi.getAuthMethods.addAll(cp.authMethods.map {authMethodToGenAuthMethod} asJavaCollection)
    cpi.getConnectors.addAll(cp.connectors.map {connToGenConn} asJavaCollection)
    cpi.setRatings(toRatingsType(cp.ratings))
    cpi.getUserInterfaceLang.addAll(cp.userInterfaceLang asJavaCollection)
    cpi
  }

  private[ochp] def toRatingsType(value: Option[Ratings]): RatingsType =
    value.map {
      case Ratings(max, guaranteed, voltage) =>
        val ratings = new RatingsType()
        ratings.setMaximumPower(max)
        guaranteed.foreach(ratings.setGuaranteedPower(_))
        voltage.foreach(ratings.setNominalVoltage(_))
        ratings
    }.getOrElse(null)

  private[ochp] def toRatingsOption(value: RatingsType): Option[Ratings] =
    Option(value).map { ratings =>
      Ratings(
        ratings.getMaximumPower,
        Option(ratings.getGuaranteedPower).map(_.toFloat),
        Option(ratings.getNominalVoltage).map(_.toInt))
    }

  implicit def toDateTimeType(date: DateTime): DateTimeType = {
      val genTtl = new DateTimeType()
      genTtl.setDateTime(date.withZone(DateTimeZone.UTC).toString(ISODateTimeFormat.dateTimeNoMillis()))
      genTtl
  }

  implicit def toEvseStatus(s: GetEvseStatusType): Option[EvseStatus] = Try {
    EvseStatus(
      evseId = EvseId(s.getEvseId),
      majorStatus = EvseStatusMajor.findByName(s.getMajor).getOrElse(EvseStatusMajor.unknown),
      minorStatus = Option(s.getMinor).flatMap(EvseStatusMinor.findByName))
  } match {
    case Success(x) => Some(x)
    case Failure(e) => logger.error("Evse status conversion failure", e); None
  }

}
