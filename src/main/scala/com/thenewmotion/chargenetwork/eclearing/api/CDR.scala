package com.thenewmotion.chargenetwork.eclearing.api

import com.thenewmotion.time.Imports._
import org.joda.time.format.ISODateTimeFormat

/**
 * Created with IntelliJ IDEA.
 * User: czwirello
 * Date: 11.09.14
 */
case class CDR(
  cdrId: String,
  evseId: String,
  emtId: EmtId,
  contractId: String,
  liveAuthId: Option[String] = None,
  status: CdrStatus.Value,
  startDateTime: DateTime,
  endDateTime: DateTime,
  duration: Option[String] = None,
  houseNumber: Option[String] = None,
  address: Option[String] = None,
  zipCode: Option[String] = None,
  city: Option[String] = None,
  country: String,
  chargePointType: String,
  connectorType: Connector,
  maxSocketPower: Float,
  productType: Option[String] = None,
  meterId: Option[String] = None,
  chargingPeriods: List[CdrPeriod])


case class CdrPeriod (
  startDateTime: DateTime,
  endDateTime: DateTime,
  billingItem: BillingItem.Value,
  billingValue: Float,
  currency: String,
  itemPrice: Float,
  periodCost: Option[Float]
)

object BillingItem extends Enumeration{
  type BillingItemType = Value
  val parkingtime = Value("parkingtime")
  val usagetime = Value("usagetime")
  val energy = Value("energy")
  val power = Value("power")
  val serviceFee = Value("serviceFee")

}