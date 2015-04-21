resolvers += "TNM" at "http://nexus.thenewmotion.com/content/repositories/releases-public"

// https://bitbucket.org/thenewmotion/sbt-build-seed
addSbtPlugin("com.thenewmotion" % "sbt-build-seed" % "0.6.1" )

// https://github.com/ebiznext/sbt-cxf-wsdl2java
addSbtPlugin("com.ebiznext.sbt.plugins" % "sbt-cxf-wsdl2java" % "0.1.4")

addSbtPlugin("com.thenewmotion" % "sbt-soapui-mockservice" % "0.1.3")