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

- Checks first if the auction that the user is trying to create do not already exist in the DHT
- If not, it creates the auction object using all the parameters received
- Search for the list of auction names in the DHT and, if found, the name is added to it and the DHT is updated
- In the end the new auction object is added to the DHT

```checkAuction```: to check the status of a specific auction
```
Value:

- String auction_name: The name of the auction
```
How it Works:

- First of all it checks if there is the list of auctions in the DHT
- If there is a positive result, takes the list, otherwise it creates a new one
- Then it looks for an auction with the name given as input and if found, takes the corresponding element
- At that point there is the data checking, to understand if an auction is active or not and at least output its status

```placeAbid```: to bid over an auction.
```
Values:

- String auctionName: The name of the auction
- Double bid: The amount of money offered
```

How it Works:

- Checks if there is the list of auctions in the DHT, if not it creates it
- Checks if the corresponding auction exists, and if found, download it from the DHT
- Then there is the time checking part, to understand if the auction is active or not
- Then, since it would not be possible, is checked if the bidder is the auction creator or the current best bidder
- If these controls are passed, the amount bid is checked, to know if it is enough to participate regoularly and if so, all the information are finally updated in the DHT

## Other methods implemented

* CheckAllAuction:
This method let the user to check all the active auction at the moment, receiving all the important information about their status

* AuctionOwner:
This method let the peer using it, have a list of all the active auction it owns as creator, receiving the name of each one

* FindAuctionByCategory:
This method let the user have a list of all the active auctions belonging to the same category

* FindAuctionByPrice:
This method let the user have a list of all the active auctions having the same price

* Message:
Al clearly explains the name, this method is used to send a message. It is used in the situations in which a better bid is made, in this case the old best offerer is notified. It is also used when the owner of an auction, suddenly decide to leave the network or remove that specific auction. in this case all the participants are notified 

* RemoveAnAuction:
This method let the author of an auction remove it from the DHT whenever he wants. All the participants to the auction will be updated about the removing with a message. Is it clear, that only the creator  of the auction can make this operation

* LeaveNetwork:
This method let any peers to leave the network, and before leaving it will also remove from the DHT all the auctions the peers own

## Testing
For the testing phase, three specific classes has been made and all of them explores all the possible outputs. 

The first two are structured examining the outputs of each method, case per case
The second one is a simple simulation  

Here all the single tests made:

``` First Testing Class ```

* A_creation 
* B_creationNoWorkDate 
* C_creationNoWorkOtherwise 
* D_checkNoWinner 
* E_checkNoParticipants 
* F_checkFailure 
* G_findingByCategory
* H_removingProcess 
* I_removingProcessWrong 
* J_whatPeersOwn 
* K_findingByPrice 


``` Second Testing Class ```

* L_checkAnotherOneWon 
* M_checkCallingPeerWon 
* N_checkYouAreHigherBidder 
* O_checkPeerHigherBidder 
* P_bidOnAlreadyWonAuction 
* Q_bidMainBidder 
* R_bidCreator 
* S_bidOnEndedAuction 
* T_bidProcess 
* U_bidNotEnough 


``` Simulation  ```
```
//First Step:
            //Creation attempts of a first auction at which multiple users take part to.
            //Peer_1 tries to create an auction, but fails since put a wrong date
            //The master creates the auction correctly
            //Peer_1 starts becoming the highest bidder
            //Peer_2 try to bid offering not enough to participate to the auction
            //Peer_3 offers more than Peer_1
            //The master tries to bid, but it can't participate to the auction since it's the owner
```
```
 //Second Step:
            //Master, peer_1 and peer_2 creates, multiple auctions
            //Peer_3 decides to check a specific auction
            //Peer_3 bid on that auction and then check it again
            //Peer_2 check the same auction on which peer_3 bid
            //Peer_2 tries to bid on Peer_3's auction, but unfortunatly for it, it's ended
            //Peer_1 checks on an auction of its interest to understand if it's still on
            //Peer_2 checks the auction on which it didn't bid in time
            //Peer_1 checks a specific category of auctions
            //Peer_2 checks the list of auctions it's the owner of
```
```
//Third Step:
            //Peer_1 checks auction of a certain price
            //Some of them try to remove, auctions not of their own
            //Master removes its own auction
            //To prove it is correct, peer_2 performs a check of all the auctions again
```

# The Dockerfile
Here is how the project Dockerfile has been structured:
```
FROM alpine/git as clone
ARG url
WORKDIR /app
RUN git clone ${url}

FROM maven:3.5-jdk-8-alpine as builder
ARG project
WORKDIR /app
COPY --from=clone /app/${project} /app
RUN mvn package
RUN mvn test -Dtest=AuctionMechanismImplSimulation

FROM openjdk:8-jre-alpine
ARG project
ENV artifact ${project}-1.0-jar-with-dependencies.jar
WORKDIR /app
ENV MASTERIP=127.0.0.1
ENV ID=0
COPY --from=builder /app/target/${artifact} /app

CMD /usr/bin/java -jar ${artifact} -m $MASTERIP -id $ID
```

Any app whose source code is hosted on GitHub, that uses Maven as compilation tool and whose output is a executable jar file, can be build following the structure of the file given.

Parameters used are:
```
- The URL of the GitHub repository
- The name of the project
```


# How to build the Project
### The Docker
First operation to perform is building the docker container in the terminal using this instruction:
```
docker build --build-arg url=https://github.com/Ciano1996/AuctionMechanism.git --build-arg project=AuctionMechanism -t auctionmechanism --no-cache .

```
### The Master Peer
Next, is necessary to start the master peer with the following instruction in interactive "-i" mode and with 2 environment variables "-e"
```
docker run -i --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 auctionmechanism
```
The MASTERIP variable refers to the master peer address, while the ID refers to the unique peer ID value. The master have to start with ID value 0

### Other Peers
After the master, other peers can be started with
```
docker run -i --name PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 auctionmechanism
```
What is important to make it work, is to change the ID values, giving each peer a different unique one




