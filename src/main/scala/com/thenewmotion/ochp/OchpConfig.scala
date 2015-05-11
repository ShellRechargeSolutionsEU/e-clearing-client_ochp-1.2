package com.thenewmotion.ochp


case class OchpConfig(
  wsUri: String,
  liveWsUri: String = "",
  user: String,
  password: String
)

