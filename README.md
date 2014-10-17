# E-Clearing client [![Build Status](https://secure.travis-ci.org/thenewmotion/e-clearing-client_ochp-1.2.png)](http://travis-ci.org/thenewmotion/e-clearing-client_ochp-1.2)

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
    val service = new EclearingService {
      val conf = EclearingConfig(
        wsUri = "http://localhost:8088/mockeCHS-OCHP_1.2",
        user = "me",
        password = "mypass"
      )
      val client = EclearingClient.createCxfClient(conf)
    }
    ```

## Setup

1. Add this repository to your pom.xml:
    ```xml
    <repository>
        <id>thenewmotion</id>
        <name>The New Motion Repository</name>
        <url>http://nexus.thenewmotion.com/content/repositories/releases-public</url>
    </repository>
    ```

2. Add dependency to your pom.xml:
    ```xml
    <dependency>
        <groupId>com.thenewmotion.chargenetwork</groupId>
        <artifactId>e-clearing-client_ochp-1.2_2.10</artifactId>
        <version>1.4</version>
    </dependency>
    ```