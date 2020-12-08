# AuctionMechanism

An auction mechanism based on P2P Network. Each peer can sell and buy goods using a Second-Price Auctions (EBay). Each bidder places a bid. The highest bidder gets the first slot, but pays the price bid by the second-highest bidder. The systems allows the users to create new auction (with an ending time, a reserved selling price and a description), check the status of an auction, and eventually place new bid for an auction. As described in the AuctionMechanism Java API.
```
Author: Francesco Ciano 
```
# Technologies involved
- Java 8
- Tom P2P
- JUnit
- Apache Maven
- Docker
- IntelliJ

# Project Structure
The POM file has been modified using MAVEN, inserting dependencies to Tom P2P.
Here structure:
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>AuctionCiano</artifactId>
    <version>1.0</version>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <repository>
            <id>tomp2p.net</id>
            <url>http://tomp2p.net/dev/mvn/</url>
        </repository>
    </repositories>

    <dependencies>

        <dependency>
            <groupId>net.tomp2p</groupId>
            <artifactId>tomp2p-all</artifactId>
            <version>5.0-Beta8</version>
        </dependency>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>3.8.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>args4j</groupId>
            <artifactId>args4j</artifactId>
            <version>2.33</version>
        </dependency>

        <dependency>
            <groupId>org.beryx</groupId>
            <artifactId>text-io</artifactId>
            <version>3.3.0</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>RELEASE</version>
            <scope>compile</scope>
        </dependency>
    </dependencies>


    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.5.1</version>
                <configuration>
                    <source>8</source>
                    <target>8</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                        <configuration>
                            <archive>
                                <manifest>
                                    <mainClass>
                                        it.unisa.implementation.Main
                                    </mainClass>
                                </manifest>
                            </archive>
                            <descriptorRefs>
                                <descriptorRef>jar-with-dependencies</descriptorRef>
                            </descriptorRefs>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>


</project>
```
The ```src/main/java/it.unisa.implementation``` package provides the following Java classes:
- Auction: The class representing the Auction object
- AuctionMechanism : The interface defining the main methods 
- AuctionMechanismImpl: The implementation of the AuctionMechanism interface
- Main: an Example of the AuctionMechanismImpl application 
- MessageListener: A simple interface for the listener of messages that peers receives

# Project Development
## Auction Class

- *Date stop_time : The deadline of the auction*
- *int owner : The owner of the auction*
- *String name : The name of the auction*
- *String category : The category at which the product belongs*
- *String description : A simple description of the product*
- *Double start_price : The starting price of the product*
- *int id_bid : The id of the winning participant*
- *Double winBid : The winning bid*
- *Double secondBid : The second highest bid*
- *PeerAddress peerAddress_bid : The id of the highest bidder*
- *PeerAddress peerAddress_oldBid : The id of the second highest bidder*
- *HashSet<PeerAddress> users : All the participants to the auction*

## AuctionMecanism Interface
Consists of the following methods:

1. createAuction: to create an auction
It takes the following values:
- String auction_name: The name of the auction
- Double start_price: The beginning price of the auction
- String category: The category of the auction
- String description: A simple description of the auction
- Date end_time: The deadline of the auction

2. checkAuction: to check the status of a specific auction
It takes the following value:
- String auction_name: The name of the auction

3. placeAbid: to bid over an auction.
It takes the following values: 
- String auctionName: The name of the auction
- Double bid: The amount of money offered

## AuctionMechanismImpl Class


## Main


## MessageListener Interface
