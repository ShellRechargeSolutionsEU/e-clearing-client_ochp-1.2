package com.thenewmotion.ochp
package client

import api._
import org.specs2.mutable.Specification
import org.specs2.specification.Scope


/**
 * OchpLiveClient integration tests.
 * need a soapUI mock service running on 8088.
 * The respective soapui project can be found in
 * it/resources/soapui/OCHP-1-3-soapui-project.xml (SoauUI v5.1.2;
 * but seem to work with 5.0.0, too)
 *
 * The mock service will be starting automatically during the pre-integration-test phase
 * so this test can run during integration-test phase
 *
 */

class OchpLiveClientSpecIT extends Specification {
  args(sequential = true)

  "OCHP live client" should {

    "get evse status" >> new TestScope {
      pending

      val result = liveClient.getStatus()
      result.size > 0
    }

    "update evse status" >> new TestScope {
      pending

      val evseStats = List(
        EvseStatus(
          EvseId("DE*823*E1234*5678"),
          EvseStatusMajor.available,
          Some(EvseStatusMinor.reserved)
        ),
        EvseStatus(
          EvseId("DE*823*E1234*6789"),
          EvseStatusMajor.`not-available`,
          Some(EvseStatusMinor.blocked)
        )
      )
      val result = liveClient.updateStatus(evseStats, Some(DateTimeNoMillis("2014-07-14T00:00:00Z")))
      result.status === ResultCode.success
    }
  }

  class TestScope extends Scope {
    val conf = new OchpConfig(
      wsUri = "http://localhost:8088/mockeCHS-OCHP_1.3",
      liveWsUri = "http://localhost:8088/mockeCHS-OCHP_1.3/live",
      user = "backend.tnm",
      password = "123456")

    val liveClient = OchpClient.createCxfLiveClient(conf)
  }
}