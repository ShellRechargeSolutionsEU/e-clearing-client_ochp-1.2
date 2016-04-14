import sbt._

val cxfVersion = "3.1.6"
val specsVersion = "3.6"

val ochp = (project in file("."))
  .enablePlugins(OssLibPlugin)
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    cxf.settings,
    soapui.settings,

    organization := "com.thenewmotion",
    name := "ochp-client-1.2",
    moduleName := name.value,

    libraryDependencies ++= Seq(
      "com.sun.xml.messaging.saaj" % "saaj-impl" % "1.3.25",
      "org.apache.cxf" %  "cxf-rt-frontend-jaxws" % cxfVersion,
      "org.apache.cxf" %  "cxf-rt-transports-http" % cxfVersion,
      "org.apache.cxf" %  "cxf-rt-ws-security" % cxfVersion,
      "com.thenewmotion" %% "time" % "2.8",
      "org.slf4j" % "slf4j-api" % "1.7.21",
      "org.specs2" %% "specs2-junit" % "3.6" % "it,test",
      "org.specs2" %% "specs2-mock" % "3.6" % "it,test"
    ),

    cxf.cxfVersion := cxfVersion,
    cxf.wsdls := Seq(
      cxf.Wsdl(
        (resourceDirectory in Compile).value / "wsdl" / "ochp-1.2.wsdl",
        Seq("-validate", "-xjc-verbose"), "ochp")
    ),
    soapui.mockServices := Seq(
      soapui.MockService(
        (resourceDirectory in IntegrationTest).value / "soapui" / "E-Clearing-soapui-project.xml",
        "8088")
    )
)
