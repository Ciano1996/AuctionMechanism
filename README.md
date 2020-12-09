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
Variables used:

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
 Takes the following values:
 
- String auction_name: The name of the auction
- Double start_price: The beginning price of the auction
- String category: The category of the auction
- String description: A simple description of the auction
- Date end_time: The deadline of the auction

### Explaination and Implementation
The method is developed as follows:

- Checks first if the auction taht the user is trying to create, do not already exist in the DHT
- If not, it create the auction object using all the parameters received
- Serch for the list of auction names in the DHT and, if found, the name is added to it and the DHT is updated
- In the end the new auction obect is added to the DHT

```
public boolean createAuction(String auction_name, Double start_price, String category, String description, Date end_time) throws IOException, ClassNotFoundException{

        if(checkAuction(auction_name) == null) {
            Date time_now = new Date();
            if (time_now.after(end_time)) {
                return false;
            }

            Auction myAuction = new Auction(peer_id, auction_name, category, description, start_price, end_time);

            FutureGet futureGet = _dht.get(Number160.createHash("auctionList")).start();
            futureGet.awaitUninterruptibly();
            if (futureGet.isSuccess()) {
                try {
                    auctionNameList = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
                }catch (NoSuchElementException e){
                }
                }

            auctionNameList.add(auction_name);

            _dht.put(Number160.createHash("auctionList")).data(new Data(auctionNameList)).start().awaitUninterruptibly();
            _dht.put(Number160.createHash(auction_name)).data(new Data(myAuction)).start().awaitUninterruptibly();

            return true;
        }
        return false;
    }
```


2. checkAuction: to check the status of a specific auction
Takes the following value:

- String auction_name: The name of the auction

### Explaination and Implementation
The method is developed as follows:

- First of all it checks if the name the user is looking for is in the DHT
-

```
 public String checkAuction(String auction_name) throws IOException, ClassNotFoundException{

        FutureGet futureGet = _dht.get(Number160.createHash(auction_name)).start();
        futureGet.awaitUninterruptibly();

        if(futureGet.isSuccess()){
           Auction auction = new Auction();

            try {
                auction = (Auction) futureGet.dataMap().values().iterator().next().object();

            } catch(NoSuchElementException e){
                System.out.println("CHECK AUCTION CATCH");
                System.out.println("qui");

                return null;
            }

            Date timeRN = new Date();

            if (timeRN.after(auction.getStop_time())){
                if(Double.compare(auction.getStart_price(), auction.getWinBid())==0){
                    return "The auction had no winner";
                } else {
                    if(auction.getId_bid() == peer_id) {
                        return "You won the auction " + auction.getName() + " bidding " + auction.getWinBid() + " and paying " + auction.getSecondBid();
                    } else
                        return "The auction winner is " + auction.getId_bid() + " bidding " + auction.getWinBid() + " and paying " + auction.getSecondBid();
                }
            } else {
                if(auction.getUsers().isEmpty()){
                    return "The auction has no participant right now, starting with a price of " + auction.getStart_price() + " € " + "and is up untill " + auction.getStop_time();
                } else {
                    if(auction.getId_bid()==peer_id){
                        return "the auction is active until " + auction.getStop_time() + "and right now you made the highest offer bidding " + auction.getWinBid();
                    } else
                        return "the auction is active until " + auction.getStop_time() + "and right now the highest offer is " + auction.getWinBid();
                }
            }
        }

        return null;
    }
```

3. placeAbid: to bid over an auction.
Takes the following values:

- String auctionName: The name of the auction
- Double bid: The amount of money offered

### Explaination and Implementation


```
public String placeABid(String auctionName, Double bid) throws IOException, ClassNotFoundException {

        FutureGet futureGet = _dht.get(Number160.createHash(auctionName)).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();

            Date timeNow = new Date();

            if (timeNow.after(auction.getStop_time())) {
                if (Double.compare(auction.getStart_price(), auction.getWinBid()) == 0) {
                    return "It is not possible for you to bid since the auction is ended with no winner";
                } else {
                    return "You can't bid, the auction has been won by participant " + auction.getId_bid();
                }
            }

            if (auction.getOwner() == peer_id) {
                return "The auction creator can't bid";
            }

            if (auction.getId_bid() == peer_id) {
                return "You are still the highest bidder";
            }

            if (bid > auction.getWinBid()) {

                auction.setSecondBid(auction.getWinBid());
                auction.setWinBid(bid);

                auction.setPeerAddress_oldBid(auction.peerAddress_bid);
                auction.setPeerAddress_bid(peer.peerAddress());

                auction.setId_bid(peer_id);

                if (!auction.getUsers().contains(peer_id)) {
                    auction.getUsers().add(peer.peerAddress());
                    auction.setPeerAddress_bid(peer.peerAddress());
                }

                _dht.put(Number160.createHash(auctionName)).data(new Data(auction)).start().awaitUninterruptibly();
                //Send Message
                message(auctionName, 1, "Better bid made on auction " + auctionName + "\n" + "Now the winning bid corresponds to " + auction.getWinBid() + " and belongs to " + auction.getId_bid());
                return "The auction " + auctionName + " is up untill " + auction.getStop_time() + " and you are winning it bidding " + auction.getWinBid();
            } else {
                return "Not enough to win";
            }

        }

        return null;
    }
```


## Main


## MessageListener Interface
