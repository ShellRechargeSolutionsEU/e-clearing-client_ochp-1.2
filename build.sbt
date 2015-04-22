import sbt._

val cxfVersion = "3.0.4"

val root = (project in file("."))
  .enablePlugins(OssLibPlugin)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings : _*)
  .settings(cxf.settings : _*)
  .settings(soapui.settings :_*)
  .settings(
    name := "ochp-client-1.2",
    moduleName := name.value,
    organization := "com.thenewmotion",
    libraryDependencies ++= Seq(
      "com.sun.xml.messaging.saaj"      %  "saaj-impl"                  % "1.3.25",
      "org.apache.cxf"                  %  "cxf-rt-frontend-jaxws"      % cxfVersion,
      "org.apache.cxf"                  %  "cxf-rt-transports-http"     % cxfVersion,
      "org.apache.cxf"                  %  "cxf-rt-ws-security"         % cxfVersion,
      "com.thenewmotion"                %% "time"                       % "2.8",
      "com.typesafe.scala-logging"      %% "scala-logging-slf4j"        % "2.1.2",
      "com.typesafe"                    %  "config"                     % "1.2.1"               % "it,test",
      "org.specs2"                      %% "specs2-junit"               % "2.4.15"              % "it,test",
      "org.specs2"                      %% "specs2-mock"                % "2.4.15"              % "it,test"
    ),
    cxf.cxfVersion := cxfVersion,
    cxf.wsdls := Seq(
      cxf.Wsdl((resourceDirectory in Compile).value / "wsdl/ochp-1.2.wsdl", Seq("-validate", "-xjc-verbose"), "ochp")
    ),
    soapui.mockServices := Seq(
      soapui.MockService( (resourceDirectory in IntegrationTest).value / "soapui" / "E-Clearing-soapui-project.xml", "8088")
    )
)
