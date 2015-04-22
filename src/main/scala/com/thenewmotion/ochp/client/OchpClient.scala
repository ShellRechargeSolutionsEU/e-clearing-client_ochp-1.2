package com.thenewmotion.ochp
package client

import java.util
import javax.xml.namespace.QName
import javax.xml.ws.Service
import javax.xml.ws.soap.SOAPBinding
import api.{CDR, ChargePoint, EvseStatus, ChargeToken}
import eu.ochp._1._
import eu.ochp._1_2.{OCHP12, OCHP12Live}
import org.apache.cxf.endpoint.Endpoint
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor
import org.apache.wss4j.common.ConfigurationConstants
import org.apache.wss4j.dom.WSConstants
import org.joda.time.DateTime
import scala.language.postfixOps

/**
 * @param cxfClient The SOAP client generated by CXF
 */
class OchpClient(cxfClient: OCHP12) {

  import Converters._
  import scala.collection.JavaConverters._

  def setRoamingAuthorisationList(info: Seq[ChargeToken]): Result[ChargeToken] = {
    val req = new SetRoamingAuthorisationListRequest()
    req.getRoamingAuthorisationInfoArray.addAll(info.map(implicitly[RoamingAuthorisationInfo](_)).asJava)
    val resp = cxfClient.setRoamingAuthorisationList(req)
    Result(resp.getResult.getResultCode.getResultCode, resp.getResult.getResultDescription,
      resp.getRefusedRoamingAuthorisationInfo.asScala.toList.map(implicitly[ChargeToken](_)))
  }

  def roamingAuthorisationList() = {
    val resp = cxfClient.getRoamingAuthorisationList(
      new GetRoamingAuthorisationListRequest)
    resp.getRoamingAuthorisationInfoArray.asScala.toList.map(implicitly[ChargeToken](_))
  }

  def setRoamingAuthorisationListUpdate(info: Seq[ChargeToken]): Result[ChargeToken] = {
    val req = new UpdateRoamingAuthorisationListRequest()
    require(info.nonEmpty, "need at least one ChargeToken to send!")
    req.getRoamingAuthorisationInfoArray.addAll(info.map(implicitly[RoamingAuthorisationInfo](_)).asJava)
    val resp = cxfClient.updateRoamingAuthorisationList(req)
    Result(resp.getResult.getResultCode.getResultCode, resp.getResult.getResultDescription,
        resp.getRefusedRoamingAuthorisationInfo.asScala.toList.map(implicitly[ChargeToken](_)))
  }

  def roamingAuthorisationListUpdate(lastUpdate: DateTime) = {
    val req = new GetRoamingAuthorisationListUpdatesRequest
    req.setLastUpdate(toDateTimeType(lastUpdate))
    val resp = cxfClient.getRoamingAuthorisationListUpdates( req )
    resp.getRoamingAuthorisationInfo.asScala.toList.map(implicitly[ChargeToken](_))
  }

  def getCdrs() = {
    val resp: GetCDRsResponse = cxfClient.getCDRs(
      new GetCDRsRequest)
    resp.getCdrInfoArray.asScala.toList.map(implicitly[CDR](_))
  }

  def addCdrs(cdrs: Seq[CDR]) = {
    val req: AddCDRsRequest = new AddCDRsRequest()
    req.getCdrInfoArray.addAll(cdrs.map(implicitly[CDRInfo](_)).asJava)
    val resp = cxfClient.addCDRs(req)
    Result[CDR](resp.getResult.getResultCode.getResultCode, resp.getResult.getResultDescription,
      resp.getImplausibleCdrsArray.asScala.toList.map(implicitly[CDR](_)))
  }

  def confirmCdrs(approvedCdrs: Seq[CDR], declinedCdrs: Seq[CDR]) = {
    val req = new ConfirmCDRsRequest()
    req.getApproved.addAll(approvedCdrs.map(implicitly[CDRInfo](_)).asJava)
    req.getDeclined.addAll(declinedCdrs.map(implicitly[CDRInfo](_)).asJava)
    val resp = cxfClient.confirmCDRs(req)
    Result(resp.getResult.getResultCode.getResultCode, resp.getResult.getResultDescription, List())
  }

  def setChargePointList(info: Seq[ChargePoint]): Result[ChargePoint] = {
    val req = new SetChargePointListRequest()
    req.getChargepointInfoArray.addAll(info.map(implicitly[ChargePointInfo](_)).asJava)
    val resp = cxfClient.setChargepointList(req)
    Result[ChargePoint](resp.getResult.getResultCode.getResultCode, resp.getResult.getResultDescription,
        resp.getRefusedChargePointInfo.asScala.toList.flatMap(implicitly[Option[ChargePoint]](_)))
  }

  def chargePointList(): Result[ChargePoint] = {
    val resp = cxfClient.getChargePointList(
      new GetChargePointListRequest)

    Result[ChargePoint](
      resp.getResult.getResultCode.getResultCode,
      resp.getResult.getResultDescription,
      resp.getChargePointInfoArray.asScala.toList.flatMap(implicitly[Option[ChargePoint]](_)))
  }

  def setChargePointListUpdate(info: Seq[ChargePoint]): Result[ChargePoint] = {
    val req = new UpdateChargePointListRequest()
    req.getChargePointInfoArray.addAll(info.map(implicitly[ChargePointInfo](_)).asJava)
    val resp = cxfClient.updateChargePointList(req)
    Result(resp.getResult.getResultCode.getResultCode, resp.getResult.getResultDescription,
      resp.getRefusedChargePointInfo.asScala.toList.flatMap(implicitly[Option[ChargePoint]](_)))
  }

  def chargePointListUpdate(lastUpdate: DateTime): Result[ChargePoint] = {
    val req = new GetChargePointListUpdatesRequest
    req.setLastUpdate(toDateTimeType(lastUpdate))
    val resp = cxfClient.getChargePointListUpdates(req)

    Result(
      resp.getResult.getResultCode.getResultCode,
      resp.getResult.getResultDescription,
      resp.getChargePointInfoArray.asScala.toList.flatMap(implicitly[Option[ChargePoint]](_)))
  }

}



class OchpLiveClient(cxfLiveClient: OCHP12Live) {
  import Converters._
  import scala.collection.JavaConverters._

  /**
   * Only implements setting the timeToLive for the whole list,
   * not individually.
   *
   * @param evseStats
   * @param timeToLive
   */
  def updateStatus(evseStats: List[EvseStatus], timeToLive: Option[DateTime] = None) = {
    def toStatusType(evseStat: EvseStatus): EvseStatusType = {
      val est = new EvseStatusType
      est.setEvseId(evseStat.evseId.value)
      est.setMajor(evseStat.majorStatus.toString)
      evseStat.minorStatus foreach {minStat=> est.setMinor(minStat.toString)}
      est
    }
    val req  = new UpdateStatusRequest
    req.getEvse.addAll(evseStats map toStatusType asJavaCollection)
    timeToLive foreach {ttl=>req.setTtl(toDateTimeType(ttl))}

    val resp = cxfLiveClient.updateStatus(req)
    Result(resp.getResult.getResultCode.getResultCode, resp.getResult.getResultDescription, List())
  }

  def getStatus(since: Option[DateTime] = None) = {
    val r   = new GetStatusRequest
    val req = since.fold(r){x => r.setStartDateTime(toDateTimeType(x));r}
    val resp = cxfLiveClient.getStatus(req)

    resp.getEvse.asScala.toList.flatMap(implicitly[Option[EvseStatus]](_))
  }

}

case class Result[A](status: ResultCode.Value, description: String, items: List[A])

object Result  {
  def apply[A](code: String, desc: String, items: List[A]) = {
    new Result(
      if ("ok".equalsIgnoreCase(code)) ResultCode.success else ResultCode.failure,
      desc, items
    )
  }
}

object ResultCode extends Enumeration {
  type ResultCode = Value
  val success = Value("success")
  val failure = Value("failure")
}


object OchpClient {

  // need to pass the pw to the PwCallbackHandler somehow,
  // but can't pass it to the constructor, else wss4j won't be
  // able to instantiate it
  var password = ""

  def createCxfClient(conf: OchpConfig): OchpClient = {
    require(conf.wsUri != "", "need endpoint uri!")
    val (servicePort: QName, service: Service) = createClient(conf, conf.wsUri)
    val cxfClient = addConfig(addWssHeaders(conf, service.getPort(servicePort, classOf[OCHP12])))
    new OchpClient(cxfClient)
  }

  def createCxfLiveClient(conf: OchpConfig): OchpLiveClient = {
    require(conf.liveWsUri != "", "need live endpoint uri!")
    val (servicePort: QName, service: Service) = createClient(conf, conf.liveWsUri)
    val cxfLiveClient = addConfig(addWssHeaders(conf, service.getPort(servicePort, classOf[OCHP12Live])))
    new OchpLiveClient(cxfLiveClient)
  }

  private def createClient(conf: OchpConfig, endpoint_address: String): (QName, Service) = {
    password = conf.password
    val servicePort: QName = new QName(endpoint_address, "service port")
    val service: Service = Service.create(servicePort)
    service.addPort(servicePort, SOAPBinding.SOAP11HTTP_BINDING, endpoint_address)
    (servicePort, service)
  }

  private def addWssHeaders[T](conf: OchpConfig, port: T): T = {
    val cxfEndpoint: Endpoint = ClientProxy.getClient(port).getEndpoint
    val outProps = new util.HashMap[String, Object]()
    outProps.put(ConfigurationConstants.ACTION, ConfigurationConstants.USERNAME_TOKEN)
    outProps.put(ConfigurationConstants.USER, conf.user)
    outProps.put(ConfigurationConstants.PASSWORD_TYPE, WSConstants.PW_TEXT)
    outProps.put(ConfigurationConstants.PW_CALLBACK_CLASS,
      new PwCallbackHandler().getClass.getName)
    val wssOut = new WSS4JOutInterceptor(outProps)
    cxfEndpoint.getOutInterceptors.add(wssOut)
    port
  }

  private def addConfig[T](port: T): T = {
    val client = ClientProxy.getClient(port)
    val http: HTTPConduit = client.getConduit.asInstanceOf[HTTPConduit]
    val httpClientPolicy: HTTPClientPolicy = new HTTPClientPolicy()
    httpClientPolicy.setAllowChunking(false)
    http.setClient(httpClientPolicy)
    port
  }

  import javax.security.auth.callback.{Callback, CallbackHandler}
  import org.apache.wss4j.common.ext.WSPasswordCallback

  private class PwCallbackHandler  extends CallbackHandler {

    def handle( callbacks: Array[Callback]) = {
      val pc: WSPasswordCallback  = callbacks(0).asInstanceOf[WSPasswordCallback]
      pc.setPassword(password)
    }
  }

}
