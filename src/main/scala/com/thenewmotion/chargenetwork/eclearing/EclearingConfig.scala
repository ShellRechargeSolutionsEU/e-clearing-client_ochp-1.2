package com.thenewmotion.chargenetwork.eclearing

/**
 * Created with IntelliJ IDEA.
 * User: czwirello
 * Date: 01.09.14
 */

case class EclearingConfig(

  wsUri: String,
  liveWsUri: String,
  user: String,
  password: String
) {
//  require(wsUri ne "")
//  require(user ne "")
//  require(password ne "")
}

