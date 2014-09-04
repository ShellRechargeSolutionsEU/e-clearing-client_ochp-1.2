# E-Clearing client [![Build Status](https://secure.travis-ci.org/thenewmotion/e-clearing-client_ochp-1.2.png)](http://travis-ci.org/thenewmotion/e-clearing-client_ochp-1.2)

Client for [www.e-clearing.net](http://www.e-clearing.net) written in Scala

## Includes

* Open Clearing House Protocol v1.2 generated client and bean classes with help of [scalaxb](http://scalaxb.org)

* Service-like trait to manipulate with Cards
```scala
    trait EclearingApi {
        def cards(): Seq[Card]
        def addCard(card: Card)
        def removeCard(card: Card)
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
        <version>2.9</version>
    </dependency>
```