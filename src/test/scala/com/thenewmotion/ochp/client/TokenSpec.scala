package com.thenewmotion.ochp
package client

import Converters._
import api._
import eu.ochp._1.{EmtId => GenEmtId, _}
import org.specs2.mutable.Specification

class TokenSpec extends Specification {
  "converting a Token into RoamingAuthorisationInfo and back returns the original value" >> {
    val t = ChargeToken(
      contractId = "YYABCC00000003",
      emtId = EmtId(
        tokenSubType = Some(TokenSubType.withName("mifareCls")),
        tokenId = "96B0149B4EA098BE769EFDE5BD6A7403F3A25BA0"),
      printedNumber = Some("YYABCC00000003J"),
      expiryDate = DateTimeNoMillis("2014-07-14T02:00:00+02:00"))

    roamingAuthorisationInfoToToken(tokenToRoamingAuthorisationInfo(t)) mustEqual t
  }
}