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

## Other methods implemented

### CheckAllAuction
This method let the user to check all the active auction at the moment, receiving all the important information about their status

### AuctionOwner
This method let the peer using it, have a list of all the active auction it own as creator, reveiving the important information about each one

### FindAuctionByCategory
This method let the user have a list of all the active auctions belonging to the same category

### FindAuctionByPrice
This method let the user have a list of all the active auctions having the same price

### Message
Al clearly explains the name, this method is used to send a message. It is used in the situations in which a better bid is made, in this case the old best offerer is notified. It is also used when the owner of an auction, suddenly decide to leave the network or remove that specific auction. in this case all the participants are notified 

### RemoveAnAuction
This method let the author of an auction remove it from the DHT whenever he wants. All the participants to the auction will be updated about the removing with a message. Is t clear, that only the creator  of the auction can make this operation

### LeaveNetwork
This method let any peers to leave the network, and befor leaving it will also remove from the DHT all the auctions the peers own

# Testing
For the testing phase, two specific classes has been made and both of them explores all the possible outputs. 

The first one is structured examining the outputs of each method, case per case

### CREATE AUCTION
/*TEST 1-1: createAuction()
    * @result create a new auction
    * */
 /*TEST 1-2: createAuction()
     * @result creating a new auction doesn't work due to a wrong date insertion
     * */
  /*TEST 1-3: createAuction()
     * @result creating a new auction doesn't work since the auction of that product as already been created
     * */   
     

```
    /*TEST 1-1: createAuction()
    * @result create a new auction
    * */

    @Test
    public void creation() throws IOException, ClassNotFoundException {
        System.out.println("creation is running...");
        try{
            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            assertTrue(master_peer.createAuction("Kingdom Hearts 3", (double) 15, "Videogames","A videogame published by Square Enix", data ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST 1-2: createAuction()
     * @result creating a new auction doesn't work due to a wrong date insertion
     * */

    @Test
    public void creationNoWorkDate(){
        System.out.println("creationNoWorkDate is running...");
        try {
            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2010");
            data.setHours(11);
            data.setMinutes(30);
            assertFalse(master_peer.createAuction("Kingdom Hearts 3", (double) 15, "Videogames","A videogame published by Square Enix", data ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST 1-3: createAuction()
     * @result creating a new auction doesn't work since the auction of that product as already been created
     * */

    @Test
    public void creationNoWorkOtherwise(){
        System.out.println("creationNoWorkOtherwise is running...");
        try {
            peer_1.createAuction("Kingdom Hearts 2", (double) 15, "Videogames","A videogame published by Square Enix", new Date(Calendar.getInstance().getTimeInMillis() + 1000 ) );
            Thread.sleep(3000);
            assertFalse(peer_2.createAuction("Kingdom Hearts 2", (double) 15, "Videogames","A videogame published by Square Enix", new Date(Calendar.getInstance().getTimeInMillis() + 1000 )));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
```    
    
### CHECK AUCTION

/*TEST 2-1: checkAuction()
     * @result the check of the auction reveals that it is ended without a winner
     * */
/*TEST 2-2: checkAuction()
     * @result the peer that checks the status of the auction, is the one that won the auction
     * */
 /*TEST 2-3: checkAuction()
     * @result the peer that checks the status of the auction, is not the one that won it
     * */  
/*TEST 2-4: checkAuction()
     * @result the auction checked has no participants
     * */
 /*TEST 2-5: checkAuction()
     * @result the peer that check is the higher bidder
     * */
/*TEST 2-6: checkAuction()
     * @result the peer that checks is not the higher bidder
     * */     
  /*TEST 2-7: checkAuction()
     * @result the check fails since it looks for a different product
     * */    
     
```
    /*TEST 2-1: checkAuction()
     * @result the check of the auction reveals that it is ended without a winner
     * */

    @Test
    public void checkNoWinner(){
        System.out.println("checkNoWinner is running...");
        try {

            peer_3.createAuction("AGV K1 VR46 Replica",(double) 150, "Helmets","Motorcycle equipment made by AGV", new Date(Calendar.getInstance().getTimeInMillis() + 1000));
            Thread.sleep(2000);

            assertEquals("The auction had no winner", peer_2.checkAuction("AGV K1 VR46 Replica"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST 2-2: checkAuction()
     * @result the peer that checks the status of the auction, is the one that won the auction
     * */

    @Test
    public void checkCallingPeerWon(){
        System.out.println("checkCallingPeerWon is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 1000);

            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data ));
            assertEquals("The auction AGV K1 is up untill " + data + " and you are winning it bidding 160.0", peer_1.placeABid("AGV K1", (double) 160));
            Thread.sleep(1500);
            assertEquals("You won the auction AGV K1 bidding 160.0 and paying 150.0", peer_1.checkAuction("AGV K1"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST 2-3: checkAuction()
     * @result the peer that checks the status of the auction, is not the one that won it
     * */

    @Test
    public void checkAnotherOneWon(){
        System.out.println("checkAnotherOneWon is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data ));
            assertEquals("The auction AGV K1 is up untill " + data + " and you are winning it bidding 160.0", peer_1.placeABid("AGV K1", (double) 160));
            Thread.sleep(1500);
            assertEquals("The auction winner is 1 bidding 160.0 and paying 150.0", peer_2.checkAuction("AGV K1"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST 2-4: checkAuction()
     * @result the auction checked has no participants
     * */

    @Test
    public void checkNoParticipants(){
        System.out.println("checkNoParticipants is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);new Date(Calendar.getInstance().getTimeInMillis() + 2000);

            peer_3.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            Thread.sleep(7000);
            assertEquals("The auction has no participant right now, starting with a price of 150.0 € and is up untill " + data, peer_2.checkAuction("AGV K1"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST 2-5: checkAuction()
     * @result the peer that check is the higher bidder
     * */

    @Test
    public void checkYouAreHigherBidder(){
        System.out.println("checkYouAreHigherBidder is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data ));
            assertEquals("The auction AGV K1 is up untill " + data + " and you are winning it bidding 160.0", peer_1.placeABid("AGV K1", (double) 160));
            assertEquals("the auction is active until " + data + "and right now you made the highest offer bidding 160.0", peer_1.checkAuction("AGV K1"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST 2-6: checkAuction()
     * @result the peer that checks is not the higher bidder
     * */

    @Test
    public void checkPeerHigherBidder(){
        System.out.println("checkPeerHigherBidder is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data ));
            assertEquals("The auction AGV K1 is up untill " + data + " and you are winning it bidding 160.0", peer_1.placeABid("AGV K1", (double) 160));
            assertEquals("the auction is active until " + data + "and right now the highest offer is 160.0", peer_2.checkAuction("AGV K1"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST 2-7: checkAuction()
     * @result the check fails since it looks for a different product
     * */

    @Test
    public void checkFailure(){
        System.out.println("checkFailure is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data));
            assertEquals(null, peer_1.checkAuction("AGV K5"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
```
### PLACE A BID

 /*TEST 3-1: placeABid()
     * @result the peer tries to bid on an already ended no won auction
     * */
  /*TEST 3-2: placeABid()
     * @result the peer tries to bid on an already won auction
     * */    
/*TEST 3-3: placeABid()
     * @result the creator of the auction tries to bid, but he can't since it's the owner
     * */
/*TEST 3-4: placeABid()
     * @result a peer bids more than once, and knows it's still the main bidder
     * */     
/*TEST 3-5: placeABid()
     * @result simple bidding mechanism result
     * */     
/*TEST 3-6: placeABid()
     * @result the peer tries to bid an amount of money less than the starting price
     * */


```
    /*TEST 3-1: placeABid()
     * @result the peer tries to bid on an already ended no won auction
     * */

    @Test
    public void bidOnEndedAuction(){
        System.out.println("bidOnEndedAuction is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data ));
            Thread.sleep(1500);
            assertEquals("It is not possible for you to bid since the auction is ended with no winner", peer_1.placeABid("AGV K1", (double) 160));



        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
    /*TEST 3-2: placeABid()
     * @result the peer tries to bid on an already won auction
     * */

    @Test
    public void bidOnAlreadyWonAuction(){
        System.out.println("bidOnAlreadyWonAuction is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 1000);

            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data ));
            assertEquals("The auction AGV K1 is up untill " + data + " and you are winning it bidding 160.0", peer_2.placeABid("AGV K1", (double) 160));
            Thread.sleep(1500);
            assertEquals("You can't bid, the auction has been won by participant 2", peer_1.placeABid("AGV K1", (double) 170));


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
    /*TEST 3-3: placeABid()
     * @result the creator of the auction tries to bid, but he can't since it's the owner
     * */

    @Test
    public void bidCreator(){
        System.out.println("bidCreator is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 1000);

            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data ));
            assertEquals("The auction creator can't bid", master_peer.placeABid("AGV K1", (double) 160));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
    /*TEST 3-4: placeABid()
     * @result a peer bids more than once, and knows it's still the main bidder
     * */

    @Test
    public void bidMainBidder(){
        System.out.println("bidMainBidder is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 4000);

            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data ));
            assertEquals("The auction AGV K1 is up untill " + data + " and you are winning it bidding 160.0", peer_1.placeABid("AGV K1", (double) 160));
            Thread.sleep(1000);
            assertEquals("You are still the highest bidder", peer_1.placeABid("AGV K1", (double) 170));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
    /*TEST 3-5: placeABid()
     * @result simple bidding mechanism result
     * */

    @Test
    public void bidProcess(){
        System.out.println("bidProcess is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 3000);

            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data ));
            assertEquals("The auction AGV K1 is up untill " + data + " and you are winning it bidding 160.0", peer_1.placeABid("AGV K1", (double) 160));


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
    /*TEST 3-6: placeABid()
     * @result the peer tries to bid an amount of money less than the starting price
     * */

    @Test
    public void bidNotEnough(){
        System.out.println("biddingNotEnough is running...");
        try {
            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 3000);
            assertTrue(master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data ));
            assertEquals("Not enough to win", peer_1.placeABid("AGV K1", (double) 100));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

```
### AUCTION OWNER

/*TEST 4-1: auctionOwner()
     * @result multiple peers creates multiple auctions and some of them wants to check what they own
     * */

```
    /*TEST 4-1: auctionOwner()
     * @result multiple peers creates multiple auctions and some of them wants to check what they own
     * */

    @Test
    public void whatPeersOwn(){
        System.out.println("whatPeersOwn is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            master_peer.createAuction("Uzumaki", (double) 50, "Comics", "A famous manga written and drawn by Junji Ito",data);
            peer_1.createAuction("Xiaomi Airdots 2", (double) 20, "Headphones","Bluetooth headphones made by Xiaomi", data);
            peer_1.createAuction("Airpods 2 Generation", (double) 120, "Headphones","Bluetooth headphones made by Apple", data );
            peer_2.createAuction("The Killing Joke", (double) 20, "Comics", "A Batman comic book", data );

            Thread.sleep(7000);

            Auction element = new Auction(0, "AGV K1", "Helmets","Motorcycle equipment made by AGV",(double) 150, data);
            Auction element1 = new Auction(0, "Uzumaki","Comics" , "A famous manga written and drawn by Junji Ito", (double) 50 , data);
            Auction element2 = new Auction(2,"The Killing Joke", "Comics","A Batman comic book",(double) 20, data);
            Auction element3 = new Auction (1, "Xiaomi Airdots 2", "Headphones","Bluetooth headphones made by Xiaomi",(double) 20, data );
            Auction element4 =new Auction(1,"Airpods 2 Generation","Headphones", "Bluetooth headphones made by Apple", (double) 120 ,data);

            ArrayList<String> list = new ArrayList<String>();
            ArrayList<String> list2 = new ArrayList<String>();
            ArrayList<String> list3 = new ArrayList<String>();


            list.add(element.getName());
            list.add(element1.getName());

            list2.add(element3.getName());
            list2.add(element4.getName());

            list3.add(element2.getName());


            assertEquals(list, master_peer.auctionOwner());
            assertEquals(list2, peer_1.auctionOwner());
           // assertEquals(list3,peer_2.auctionOwner());

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
```

### CHECK ALL AUCTION

/*TEST 5-1: checkAllAuction()
     * @result multiple peers creates multiple auctions and some of them check all the available auctions
     * */

```
    /*TEST 5-1: checkAllAuction()
     * @result multiple peers creates multiple auctions and some of them check all the available auctions
     * */

    @Test
    public void checkingAll(){
        System.out.println("checkingAll is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            peer_3.createAuction("The Dark Knight Return", (double) 20, "Comics", "A Batman comic book", data );
            peer_1.createAuction("Xiaomi Airdots PRO", (double) 20, "Headphones","Bluetooth headphones made by Xiaomi", data);
            peer_2.createAuction("Airpods 1 Generation", (double) 120, "Headphones","Bluetooth headphones made by Apple", data );

            Auction element2 = new Auction(3,"The Dark Knight Return", "Comics","A Batman comic book",(double) 20, data);
            Auction element3 = new Auction (1, "Xiaomi Airdots PRO", "Headphones","Bluetooth headphones made by Xiaomi",(double) 20, data);
            Auction element4 =new Auction(2,"Airpods 1 Generation","Headphones", "Bluetooth headphones made by Apple", (double) 120 ,data);

            Thread.sleep(7000);

            ArrayList<Auction> list = new ArrayList<Auction>();

            list.add(element2);
            list.add(element3);
            list.add(element4);

            assertEquals(list.toString(), peer_1.checkAllAuction().toString());
            assertEquals(list.toString(), peer_2.checkAllAuction().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

```
### FIND AUCTION BY CATEGORY

/*TEST 6-1: findAuctionByCategory()
     * @result multiple peers creates multiple auctions and some of them wants to check what belongs to a certain category
     * */

```
    /*TEST 6-1: findAuctionByCategory()
     * @result multiple peers creates multiple auctions and some of them wants to check what belongs to a certain category
     * */

    @Test
    public void findingByCategory(){
        System.out.println("findingByCategory is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            master_peer.createAuction("Uzumaki", (double) 50, "Comics", "A famous manga written and drawn by Junji Ito",data);
            peer_1.createAuction("Xiaomi Airdots", (double) 20, "Headphones","Bluetooth headphones made by Xiaomi", data);
            peer_1.createAuction("Airpods", (double) 120, "Headphones","Bluetooth headphones made by Apple", data );

            Thread.sleep(7000);

            Auction element = new Auction(0, "AGV K1", "Helmets","Motorcycle equipment made by AGV",(double) 150, data);
            Auction element1 = new Auction(0, "Uzumaki","Comics" , "A famous manga written and drawn by Junji Ito", (double) 50 , data);
            Auction element3 = new Auction (1, "Xiaomi Airdots", "Headphones","Bluetooth headphones made by Xiaomi",(double) 20, data);
            Auction element4 =new Auction(1,"Airpods","Headphones", "Bluetooth headphones made by Apple", (double) 120 ,data);

            ArrayList<Auction> list = new ArrayList<Auction>();
            ArrayList<Auction> list2 = new ArrayList<Auction>();

            list.add(element1);

            list2.add(element3);
            list2.add(element4);

            Thread.sleep(2000);

            assertEquals(list.toString(), master_peer.findAuctionByCategory("Comics").toString());
            assertEquals(list2.toString(), peer_3.findAuctionByCategory("Headphones").toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
```
### FIND AUCTION BY PRICE

/*TEST 7-1: findAuctionByPrice()
     * @result multiple peers creates multiple auctions and some of them wants to check which of them have a certain price
     * */

```
    /*TEST 7-1: findAuctionByPrice()
     * @result multiple peers creates multiple auctions and some of them wants to check which of them have a certain price
     * */

    @Test
    public void findingByPrice(){
        System.out.println("findingByPrice is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            master_peer.createAuction("Gyon", (double) 50, "Comics", "A famous manga written and drawn by Junji Ito",data);
            peer_1.createAuction("Xiaomi Airdots 1", (double) 20, "Headphones","Bluetooth headphones made by Xiaomi", data);
            peer_2.createAuction("The Laughing Bat", (double) 25, "Comics", "A Batman comic book", data );

            Thread.sleep(3000);

            Auction element3 = new Auction (1, "Xiaomi Airdots 1", "Headphones","Bluetooth headphones made by Xiaomi",(double) 20, data);

            ArrayList<Auction> list = new ArrayList<Auction>();
            list.add(element3);

            Thread.sleep(2000);

            assertEquals(list.toString(),peer_3.findAuctionByPrice((double) 20).toString());


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
```
### REMOVE AUCTION

/*TEST 8-1: removeAnAuction()
     * @result multiple peers creates multiple auctions and some of them at a certain point, decides to remove what belongs to its
     * */

```
    /*TEST 8-1: removeAnAuction()
     * @result multiple peers creates multiple auctions and some of them at a certain point, decides to remove what belongs to its
     * */

    @Test
    public void removingProcess(){
        System.out.println("removingProcess is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 4000);

            master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            master_peer.createAuction("Uzumaki", (double) 50, "Comics", "A famous manga written and drawn by Junji Ito",data);
            peer_1.createAuction("Xiaomi Airdots", (double) 20, "Headphones","Bluetooth headphones made by Xiaomi", data);
            peer_1.createAuction("Airpods", (double) 120, "Headphones","Bluetooth headphones made by Apple", data );
            peer_2.createAuction("The Killing Joke", (double) 20, "Comics", "A Batman comic book", data );

            Thread.sleep(7000);

            assertTrue(master_peer.removeAnAuction("AGV K1"));
            assertTrue(peer_1.removeAnAuction("Airpods"));
            assertTrue(peer_1.removeAnAuction("Xiaomi Airdots"));


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST 8-1: removeAnAuction()
     * @result multiple peers creates multiple auctions and some of them tries to remove someone else's creation
     * */

    @Test
    public void removingProcessWrong(){
        System.out.println("removingProcessWrong is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 4000);

            master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            master_peer.createAuction("Uzumaki", (double) 50, "Comics", "A famous manga written and drawn by Junji Ito",data);
            peer_1.createAuction("Xiaomi Airdots", (double) 20, "Headphones","Bluetooth headphones made by Xiaomi", data);
            peer_1.createAuction("Airpods", (double) 120, "Headphones","Bluetooth headphones made by Apple", data );
            peer_2.createAuction("The Killing Joke", (double) 20, "Comics", "A Batman comic book", data );

            Thread.sleep(7000);

            assertFalse(master_peer.removeAnAuction("The Killing Joke"));
            assertFalse(peer_2.removeAnAuction("Airpods"));
            assertFalse(peer_3.removeAnAuction("Xiaomi Airdots"));


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }
```



