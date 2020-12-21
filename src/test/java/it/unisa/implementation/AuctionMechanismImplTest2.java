package it.unisa.implementation;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuctionMechanismImplTest2 {

    private static AuctionMechanismImpl master_peer;
    private static AuctionMechanismImpl peer_1;
    private static AuctionMechanismImpl peer_2;
    private static AuctionMechanismImpl peer_3;
    private static AuctionMechanismImpl peer_4;


    public AuctionMechanismImplTest2() throws Exception {

        class MessageListenerImpl implements MessageListener{
            int peerid;
            public MessageListenerImpl(int peerid){
                this.peerid=peerid;
            }

            public Object parseMessage(Object obj){
                System.out.println(peerid + "] (Direct Message Received)" + obj);
                return "success";
            }
        }

        master_peer = new AuctionMechanismImpl(0, "127.0.0.1", new MessageListenerImpl(0));
        peer_1 = new AuctionMechanismImpl(1, "127.0.0.1", new MessageListenerImpl(1));
        peer_2 = new AuctionMechanismImpl(2, "127.0.0.1", new MessageListenerImpl(2));
        peer_3 = new AuctionMechanismImpl(3, "127.0.0.1", new MessageListenerImpl(3));
        peer_4 = new AuctionMechanismImpl(4, "127.0.0.1", new MessageListenerImpl(4));

    }

        @After
        public void tearDown() throws IOException, ClassNotFoundException {
        peer_1.leaveNetwork();
        peer_2.leaveNetwork();
        peer_3.leaveNetwork();
        peer_4.leaveNetwork();
        master_peer.leaveNetwork();
        }


    /*TEST : checkAuction()
     * @result the peer that checks the status of the auction, is the one that won the auction
     * */

    @Test
    public void M_checkCallingPeerWon(){
        System.out.println("checkCallingPeerWon is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 1000);

            peer_4.createAuction("AGV K3",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            peer_3.placeABid("AGV K3", (double) 160);
            Thread.sleep(1500);
            assertEquals("You won the auction AGV K3 bidding 160.0 and paying 150.0", peer_3.checkAuction("AGV K3"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST : checkAuction()
     * @result the peer that checks the status of the auction, is not the one that won it
     * */

    @Test
    public void L_checkAnotherOneWon(){
        System.out.println("checkAnotherOneWon is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 1000);

            master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            peer_1.placeABid("AGV K1", (double) 160);
            Thread.sleep(1500);
            assertEquals("The auction winner is 1 bidding 160.0 and paying 150.0", peer_3.checkAuction("AGV K1"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }


    /*TEST : checkAuction()
     * @result the peer that check is the higher bidder
     * */

    @Test
    public void N_checkYouAreHigherBidder(){
        System.out.println("checkYouAreHigherBidder is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            peer_2.createAuction("AGV K6",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            peer_1.placeABid("AGV K6", (double) 160);
            assertEquals("the auction is active until " + data + " and right now you made the highest offer bidding 160.0", peer_1.checkAuction("AGV K6"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST : checkAuction()
     * @result the peer that checks is not the higher bidder
     * */

    @Test
    public void O_checkPeerHigherBidder(){
        System.out.println("checkPeerHigherBidder is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            peer_2.createAuction("AGV K1 Special",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            peer_4.placeABid("AGV K1 Special", (double) 160);
            assertEquals("the auction is active until " + data + " and right now the highest offer is 160.0", peer_3.checkAuction("AGV K1 Special"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }




    /*TEST : placeABid()
     * @result the peer tries to bid on an already ended no won auction
     * */

    @Test
    public void S_bidOnEndedAuction(){
        System.out.println("bidOnEndedAuction is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 1000);

            peer_1.createAuction("AGV K5 Special",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            Thread.sleep(1500);
            assertEquals("It is not possible for you to bid since the auction is ended with no winner", peer_3.placeABid("AGV K5 Special", (double) 160));



        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST : placeABid()
     * @result the peer tries to bid on an already won auction
     * */

    @Test
    public void P_bidOnAlreadyWonAuction(){
        System.out.println("bidOnAlreadyWonAuction is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 3000);

            peer_1.createAuction("Icon Airflite",(double) 150, "Helmets","Motorcycle equipment made by Icon", data );
            peer_3.placeABid("Icon Airflite", (double) 160);
            Thread.sleep(3000);
            assertEquals("You can't bid, the auction has been won by participant 3", peer_4.placeABid("Icon Airflite", (double) 170));



        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST : placeABid()
     * @result the creator of the auction tries to bid, but he can't since it's the owner
     * */

    @Test
    public void R_bidCreator(){
        System.out.println("bidCreator is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 1000);

            master_peer.createAuction("AGV K1",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            assertEquals("The auction creator can't bid", master_peer.placeABid("AGV K1", (double) 160));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST : placeABid()
     * @result a peer bids more than once, and knows it's still the main bidder
     * */

    @Test
    public void Q_bidMainBidder(){
        System.out.println("bidMainBidder is running...");
        try {

            Date data =  new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            peer_2.createAuction("Vemar ZEPHIR",(double) 150, "Helmets","Motorcycle equipment made by Vemar", data );
            peer_1.placeABid("Vemar ZEPHIR", (double) 160);
            Thread.sleep(1000);
            assertEquals("You are still the highest bidder", peer_1.placeABid("Vemar ZEPHIR", (double) 170));

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST : placeABid()
     * @result simple bidding mechanism result
     * */

    @Test
    public void T_bidProcess(){
        System.out.println("bidProcess is running...");
        try {

            Date data =  new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            peer_3.createAuction("LS2 Modulare",(double) 150, "Helmets","Motorcycle equipment made by LS2", data );
            assertEquals("The auction LS2 Modulare is up untill " + data + " and you are winning it bidding 160.0", peer_4.placeABid("LS2 Modulare", (double) 160));


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST : placeABid()
     * @result the peer tries to bid an amount of money less than the starting price
     * */

    @Test
    public void U_bidNotEnough(){
        System.out.println("biddingNotEnough is running...");
        try {
            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 3000);
            peer_4.createAuction("AGV K1 VR46 Replica",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            assertEquals("Not enough to win", peer_2.placeABid("AGV K1 VR46 Replica", (double) 100));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }



}
