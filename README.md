# AuctionMechanism

An auction mechanism based on P2P Network. Each peer can sell and buy goods using a Second-Price Auctions (EBay). Each bidder places a bid. The highest bidder gets the first slot, but pays the price bid by the second-highest bidder. The systems allows the users to create new auction (with an ending time, a reserved selling price and a description), check the status of an auction, and eventually place new bid for an auction. As described in the AuctionMechanism Java API.
```
Author: Francesco Ciano 
```
## Technologies involved
- Java 8
- Tom P2P
- JUnit
- Apache Maven
- Docker
- IntelliJ

## Project Structure
The POM file has been modified using MAVEN, inserting dependencies to Tom P2P.

The ```src/main/java/it.unisa.implementation``` package provides the following Java classes:
- Auction: The class representing the Auction object
- AuctionMechanism : The interface defining the main methods 
- AuctionMechanismImpl: The implementation of the AuctionMechanism interface
- Main: an Example of the AuctionMechanismImpl application 
- MessageListener: A simple interface for the listener of messages that peers receives

## Project Development
### Auction Class
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

### AuctionMecanism
Consists of the following methods:

```createAuction```: to create an auction
 ```
Values:
 
- String auction_name: The name of the auction
- Double start_price: The beginning price of the auction
- String category: The category of the auction
- String description: A simple description of the auction
- Date end_time: The deadline of the auction
```
How it Works:

- Checks first if the auction that the user is trying to create, do not already exist in the DHT
- If not, it creates the auction object using all the parameters received
- Search for the list of auction names in the DHT and, if found, the name is added to it and the DHT is updated
- In the end the new auction object is added to the DHT

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


```checkAuction```: to check the status of a specific auction
```
Value:

- String auction_name: The name of the auction
```
How it Works:

- First of all it checks if the name the user is looking for is in the DHT
- If there is a positive result, takes the list, otherwise it create a new one
- Then it looks for an auction with the name given as input and if found, takes the corresponding element
- At that point there is the data checking, to understand if an auction is active or not and at least output its status

```
 public String checkAuction(String auction_name) throws IOException, ClassNotFoundException{

        FutureGet futureGet = _dht.get(Number160.createHash(auction_name)).start();
        futureGet.awaitUninterruptibly();

        if(futureGet.isSuccess()){
           Auction auction = new Auction();

            try {
                auction = (Auction) futureGet.dataMap().values().iterator().next().object();

            } catch(NoSuchElementException e){
               
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

```placeAbid```: to bid over an auction.
```
Values:

- String auctionName: The name of the auction
- Double bid: The amount of money offered
```

How it Works:

- Checks if the corresponding auction exists, and if found, download it from the DHT
- Then there is the time checking part, to understand if the auction is active or not
- Then, since it would not be possible, is checked if the bidder is the auction creator or the current best bidder
-If these controls are passed, the amount bid is checked, to know if it is enough to participate regoularly and if so, all the information are finally updated in the DHT
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

* CheckAllAuction:
This method let the user to check all the active auction at the moment, receiving all the important information about their status

* AuctionOwner:
This method let the peer using it, have a list of all the active auction it own as creator, reveiving the important information about each one

* FindAuctionByCategory:
This method let the user have a list of all the active auctions belonging to the same category

* FindAuctionByPrice:
This method let the user have a list of all the active auctions having the same price

* Message:
Al clearly explains the name, this method is used to send a message. It is used in the situations in which a better bid is made, in this case the old best offerer is notified. It is also used when the owner of an auction, suddenly decide to leave the network or remove that specific auction. in this case all the participants are notified 

* RemoveAnAuction:
This method let the author of an auction remove it from the DHT whenever he wants. All the participants to the auction will be updated about the removing with a message. Is t clear, that only the creator  of the auction can make this operation

* LeaveNetwork:
This method let any peers to leave the network, and befor leaving it will also remove from the DHT all the auctions the peers own

## Testing
For the testing phase, two specific classes has been made and both of them explores all the possible outputs. 

The first one is structured examining the outputs of each method, case per case
The second one is a simple simulation  

#### CREATE AUCTION
-TEST 1-1: create a new auction
-TEST 1-2: creating a new auction doesn't work due to a wrong date insertion
-TEST 1-3: creating a new auction doesn't work since the auction of that product as already been created
         
#### CHECK AUCTION

-TEST 2-1:  the check of the auction reveals that it is ended without a winner    
-TEST 2-2:  the peer that checks the status of the auction, is the one that won the auction    
-TEST 2-3:  the peer that checks the status of the auction, is not the one that won it      
-TEST 2-4: the auction checked has no participants  
-TEST 2-5: the peer that check is the higher bidder    
-TEST 2-6: the peer that checks is not the higher bidder       
-TEST 2-7: the check fails since it looks for a different product
         
#### PLACE A BID

-TEST 3-1: the peer tries to bid on an already ended no won auction   
-TEST 3-2: the peer tries to bid on an already won auction      
-TEST 3-3: the creator of the auction tries to bid, but he can't since it's the owner    
-TEST 3-4: a peer bids more than once, and knows it's still the main bidder     
-TEST 3-5: simple bidding mechanism result
-TEST 3-6: the peer tries to bid an amount of money less than the starting price
     
#### AUCTION OWNER

-TEST 4-1: multiple peers creates multiple auctions and some of them wants to check what they own

#### FIND AUCTION BY CATEGORY

-TEST 5-1: multiple peers creates multiple auctions and some of them wants to check what belongs to a certain category

#### FIND AUCTION BY PRICE

-TEST 6-1: multiple peers creates multiple auctions and some of them wants to check which of them have a certain price    

#### REMOVE AUCTION

-TEST 7-1: multiple peers creates multiple auctions and some of them at a certain point, decides to remove what belongs to its    
-TEST 7-2: multiple peers creates multiple auctions and some of them tries to remove someone else's creation

# The Dockerfile
Here is how the project Dockerfile has been structured:
```
FROM alpine/git
WORKDIR /app
RUN git clone https://github.com/Ciano1996/AuctionMechanism.git

FROM maven:3.5-jdk-8-alpine
WORKDIR /app
COPY --from=0 /app/AuctionMechanism /app
RUN mvn package

FROM openjdk:8-jre-alpine
ENV MASTERIP=127.0.0.1
ENV ID=0
COPY --from=1 /app/target/AuctionCiano-1.0-jar-with-dependencies.jar /

CMD /usr/bin/java -jar AuctionCiano-1.0-jar-with-dependencies.jar -m $MASTERIP -id $ID
```

# How to build the Project
### The Docker
First operation to perform is building the docker container in the terminal using this instruction:
```
docker build --no-cache -t auctionciano .

```
### The Master Peer
Next, is necessary to start the master peer with the following instruction in interactive "-i" mode and with 2 environment variables "-e"
```
docker run -i --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 auctionciano
```
The MASTERIP variable refers to the master peer address, while the ID refers to the unique peer ID value. The master have to start with ID value 0

### Other Peers
After the master, other peers can be started with
```
docker run -i --name PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 auctionciano
```
What is important to make it work, is to change the ID values, giving each peer a different unique one




