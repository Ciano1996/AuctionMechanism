package it.unisa.implementation;

import it.unisa.implementation.AuctionMechanismImpl;
import it.unisa.implementation.MessageListener;
import org.junit.runners.MethodSorters;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class AuctionMechanismImplTest {

    private static AuctionMechanismImpl master_peer;
    private static AuctionMechanismImpl peer_1;
    private static AuctionMechanismImpl peer_2;
    private static AuctionMechanismImpl peer_3;



    @Before
    public void setup() throws Exception{

        class MessageListenerimpl implements MessageListener{
            int peerid;
            public MessageListenerimpl(int peerid){
                this.peerid=peerid;
            }

            public Object parseMessage(Object obj){
                System.out.println(peerid + "] (Direct Message Received)" + obj);
                return "success";
            }
        }

        master_peer = new AuctionMechanismImpl(0, "127.0.0.1", new MessageListenerimpl(0));
        peer_1 = new AuctionMechanismImpl(1, "127.0.0.1", new MessageListenerimpl(1));
        peer_2 = new AuctionMechanismImpl(2, "127.0.0.1", new MessageListenerimpl(2));
        peer_3 = new AuctionMechanismImpl(3, "127.0.0.1", new MessageListenerimpl(3));
    }

        @After
        public void tearDown() throws IOException, ClassNotFoundException {
        master_peer.leaveNetwork();
        peer_1.leaveNetwork();
        peer_2.leaveNetwork();
        peer_3.leaveNetwork();
        }




// CREATE AUCTION___________________________________________________________________________________________________________

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
// CHECK AUCTION___________________________________________________________________________________________________________

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

// PLACE A BID___________________________________________________________________________________________________________

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


// AUCTION OWNER___________________________________________________________________________________________________________

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


// CHECK ALL AUCTION___________________________________________________________________________________________________________

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


// FIND AUCTION BY CATEGORY___________________________________________________________________________________________________________

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

// FIND AUCTION BY PRICE___________________________________________________________________________________________________________


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

// REMOVE AUCTION___________________________________________________________________________________________________________

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



}//class
