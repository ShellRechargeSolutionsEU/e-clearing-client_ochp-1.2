# OCHP client [![Build Status](https://secure.travis-ci.org/thenewmotion/e-clearing-client_ochp-1.2.png)](http://travis-ci.org/thenewmotion/e-clearing-client_ochp-1.2)

Client for [www.e-clearing.net](http://www.e-clearing.net) written in Scala

## Includes

* Open Clearing House Protocol v1.2 generated client and bean classes with help of [cxf](http://cxf.apache.org)

* API trait to communicate with the clearing house:
    ```scala
    trait EclearingApi {
      def recvAllTokens(): List[ChargeToken]
      def sendAllTokens(tokens: List[ChargeToken]): Result[ChargeToken]
      def recvNewTokens(lastUpdate: DateTime): List[ChargeToken]
      def sendNewTokens(tokens: List[ChargeToken]): Result[ChargeToken]
    
      def sendAllChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint]
      def recvAllChargePoints():List[ChargePoint]
      def sendNewChargePoints(chargePoints: List[ChargePoint]): Result[ChargePoint]
      def recvNewChargePoints(lastUpdate: DateTime):List[ChargePoint]
    
      def sendCdrs(cdrs: List[CDR]): Result[CDR]
      def recvCdrs(): List[CDR]
      def confCdrs(approvedCdrs: List[CDR], declinedCdrs: List[CDR])
    }

    ```
    
* Service trait that can be instantiated like here:
    ```scala
    val service = new OchpService {
      val conf = OchpConfig(
        wsUri = "http://localhost:8088/mockeCHS-OCHP_1.2",
        user = "me",
        password = "mypass"
      )
      val client = OchpClient.createCxfClient(conf)
    }
    ```

## Setup

### Maven

1. Add this repository to your pom.xml:
    ```xml
    <repository>
        <id>thenewmotion</id>
        <name>The New Motion Repository</name>
        <url>http://nexus.thenewmotion.com/content/groups/public"</url>
    </repository>
    ```

2. Add dependency to your pom.xml:
    ```xml
    <dependency>
        <groupId>com.thenewmotion</groupId>
        <artifactId>ochp-client-1.2_2.11</artifactId>
        <version>1.20</version>
    </dependency>
    ```
### SBT

1. Add the following resolver: 
    ```scala
    resolvers += "TNM" at "http://nexus.thenewmotion.com/content/groups/public"
    ```

2. Add the following dependency:
    ```scala
    "com.thenewmotion" %% "ochp-client-1.2" % "1.20",
    ```
    
