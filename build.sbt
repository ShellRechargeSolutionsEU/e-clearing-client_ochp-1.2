import sbt._

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
      "org.apache.cxf"                  %  "cxf-bundle"                 % "3.0.0-milestone2",
      "com.thenewmotion"                %% "time"                       % "2.8",
      "com.typesafe.scala-logging"      %% "scala-logging-slf4j"        % "2.1.2",
      "com.typesafe"                    %  "config"                     % "1.2.1"               % "it,test",
      "org.specs2"                      %% "specs2-junit"               % "2.4.15"              % "it,test",
      "org.specs2"                      %% "specs2-mock"                % "2.4.15"              % "it,test"
    ),
    cxf.wsdls := Seq(
      cxf.Wsdl((resourceDirectory in Compile).value / "wsdl/ochp-1.2.wsdl", Seq("-validate", "-xjc-verbose"), "ochp")
    ),
    soapui.mockServices := Seq(
      soapui.MockService( (resourceDirectory in IntegrationTest).value / "soapui" / "E-Clearing-soapui-project.xml", "8088")
    )
)
