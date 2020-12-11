package it.unisa.implementation;


import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.NoSuchFileException;
import java.util.*;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FutureRemove;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class AuctionMechanismImpl implements AuctionMechanism {

    final private Peer peer;
    final private PeerDHT _dht;
    final private int DEFAULT_MASTER_PORT = 4000;

    private static final Random RND = new Random(42L);

    int peer_id;

    private String auction_name;

    ArrayList<String> auctionNameList = new ArrayList<String>();

    public ArrayList<Auction> auctions = new ArrayList<Auction>();


    //Constructor
    public AuctionMechanismImpl(int _id, String _master_peer, final MessageListener _listener)throws Exception{

        peer= new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
        _dht = new PeerBuilderDHT(peer).start();

        FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
        fb.awaitUninterruptibly();
        if(fb.isSuccess()) {
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        }else {
            throw new Exception("Error in master peer bootstrap.");
        }
        peer_id= _id;

        peer.objectDataReply(new ObjectDataReply() {

            public Object reply(PeerAddress sender, Object request) throws Exception {
                return _listener.parseMessage(request);
            }
        });
    }



    /**
     * Creates a new auction for a good.
     * @param auction_name a String, the name identify the auction.
     * @param end_time a Date that is the end time of an auction.
     * @param start_price a double value that is the reserve minimum pricing selling.
     * @param category a String value that is the category at which the auction object belongs
     * @param description a String describing the selling goods in the auction.
     * @return true if the auction is correctly created, false if the time is not inserted correctly or otherwise.
     */

    @Override
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


    /**
     * Checks the status of the auction.
     * @param auction_name a String, the name of the auction.
     * @return a String value that is the status of the auction, null if the auction you're looking for does not exists.
     */


    @Override
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



    /**
     * Places a bid for an auction if it is not already ended.
     * @param auctionName a String, the name of the auction.
     * @param bid a double value, the bid for an auction.
     * @return a String value that is the status of the auction.
     */

    @Override
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




    /**
     * Let the calling peer know the list of auctions whose it's the creator of.
     * @return an ArrayList of auctions.
     */

    public ArrayList<String> auctionOwner() throws IOException, ClassNotFoundException {
        try {
          ArrayList<Auction> hub = checkAllAuction();

        ArrayList<String> names = new ArrayList<String>();

        for (Auction a : hub) {

                if (peer_id == a.getOwner()) {
                    names.add(a.getName());
                }
        }
        return names;
        } catch (NullPointerException e) {
            return null;
        }
    }



    /**
     * Let the calling peer exit the network.
     */

    public void leaveNetwork() throws IOException, ClassNotFoundException {
        try {
            ArrayList<String> outro = auctionOwner();

            if (!outro.isEmpty()) {
                for (String bye : outro) {
                    removeAnAuction(bye);
                    message(bye, 2, "The Auction " + bye + " has been closed since the Owner left");
                }
            }
        } catch (NullPointerException e){
        }
        _dht.peer().announceShutdown().start().awaitUninterruptibly();
    }


    /**
     * Let the calling peer know the list of all the active auctions.
     * @return an ArrayList of auctions.
     */

    public ArrayList<Auction> checkAllAuction() throws IOException, ClassNotFoundException {

        FutureGet futureGet = _dht.get((Number160.createHash("auctionList"))).start();
        futureGet.awaitUninterruptibly();

        ArrayList<Auction> allAuction = new ArrayList<Auction>();

        if (futureGet.isSuccess()) {
            if(!futureGet.dataMap().values().isEmpty()) {

                try {
                    auctionNameList = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
                    System.out.println(auctionNameList.size());
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                }
                if(!auctionNameList.isEmpty()){

                    for (String auct: auctionNameList ) {

                        Date timeRN = new Date();
                        futureGet = _dht.get(Number160.createHash(auct)).start();
                        futureGet.awaitUninterruptibly();

                        if (futureGet.isSuccess()) {
                            try {
                                Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();

                                if (!timeRN.after(auction.getStop_time())) {
                                    allAuction.add(auction);
                                }
                            } catch (NoSuchElementException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return allAuction;
    }


    /**
     * A method that let peers send message each others to notify specific events.
     */

    public void message(String auctionName, int type, Object obj) throws IOException, ClassNotFoundException {

        FutureGet futureGet = _dht.get(Number160.createHash(auctionName)).start();
        futureGet.awaitUninterruptibly();

        if (futureGet.isSuccess()) {
            try {
                Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();
                HashSet<PeerAddress> users = auction.getUsers();

                for (PeerAddress mypeer : users) {
                    if (mypeer.equals(auction.getPeerAddress_oldBid()) && users.size() > 1 && type == 1) {
                        FutureDirect futureDirect = _dht.peer().sendDirect(mypeer).object(obj).start();
                        futureDirect.awaitUninterruptibly();
                    } else if (type != 1) {
                        FutureDirect futureDirect = _dht.peer().sendDirect(mypeer).object(obj).start();
                        futureDirect.awaitUninterruptibly();
                    }
                }
            } catch (NoSuchElementException e){

            }
        }
    }

    /**
     * Let the calling peer know the list of auctions belonging to a certain category.
     * @return an ArrayList of auctions.
     */

    public ArrayList<Auction> findAuctionByCategory(String categ) throws IOException, ClassNotFoundException {

        ArrayList<Auction> results= new ArrayList<Auction>();
        ArrayList<Auction> everyAuction = checkAllAuction();

        for (Auction a : everyAuction) {
            if (a.getCategory().equals(categ)){
                results.add(a);
            }
        }

        return results;
    }

    /**
     * Let the calling peer know the list of auctions having a certain price or lower.
     * @return an ArrayList of auctions.
     */


    public ArrayList<Auction> findAuctionByPrice(Double price) throws IOException, ClassNotFoundException {

        ArrayList<Auction> results= new ArrayList<Auction>();
        ArrayList<Auction> everyAuction = checkAllAuction();

        for (Auction a : everyAuction) {
            if (a.getStart_price() <= price ){
                results.add(a);
            }
        }

        return results;

    }

    /**
     * Let the calling peer remove an auction it's the creator of.
     * @param name a String, the name of the auction.
     * @return true if the auction is removed correctly, false otherwise
     */


    public boolean removeAnAuction(String name) throws IOException, ClassNotFoundException{

        if(checkAuction(name) != null) {

            FutureGet futureGet = _dht.get(Number160.createHash(name)).start();
            futureGet.awaitUninterruptibly();

            if (futureGet.isSuccess()){
                Auction auctionToRemove = (Auction) futureGet.dataMap().values().iterator().next().object();

                if(peer_id== auctionToRemove.getOwner()){
                    FutureRemove futureRemove = _dht.remove(Number160.createHash(name)).start();
                    futureRemove.awaitUninterruptibly();
                    if (futureRemove.isSuccess()){

                        FutureGet futureGet2 = _dht.get(Number160.createHash("auctionList")).start();
                        futureGet2.awaitUninterruptibly();
                        if (futureGet2.isSuccess()) {
                           try {
                               auctionNameList = (ArrayList<String>) futureGet2.dataMap().values().iterator().next().object();
                           } catch (NoSuchElementException e){
                               return false;
                           }
                        }

                        auctionNameList.remove(name);
                        _dht.put(Number160.createHash("auctionList")).data(new Data(auctionNameList)).start().awaitUninterruptibly();
                        message(name, 2 ,"The Auction " + name + " has been removed by its Owner");
                        return true;
                    }
                }
            }
        }

        return false;
    }



} //class
