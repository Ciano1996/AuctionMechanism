package it.unisa.implementation;

import it.unisa.implementation.AuctionMechanismImpl;
import it.unisa.implementation.MessageListener;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;


public class AuctionMechanismImplSimulation {

    private static AuctionMechanismImpl master_peer;
    private static AuctionMechanismImpl peer_1;
    private static AuctionMechanismImpl peer_2;
    private static AuctionMechanismImpl peer_3;



    @BeforeClass
    public static void setup() throws Exception{

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



    @Test
    public void Case(){

        try{

            //First Step:
            //Creation attempts of a first auction at which multiple users take part to.
            //Peer_1 tries to create an auction, but fails since put a wrong date
            //The master creates the auction correctly
            //Peer_1 starts becoming the highest bidder
            //Peer_2 try to bid offering not enough to participate to the auction
            //Peer_3 offers more than Peer_1
            //The master tries to bid, but it can't participate to the auction since it's the owner


            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            Date data2 = new Date();
            data2 = formatter.parse("12/10/2010");
            data2.setHours(11);
            data2.setMinutes(30);

            assertFalse(peer_1.createAuction("Zombiecide", (double) 80, "Cardboard Game","A famous cardboard game", data2 ));
            assertTrue(master_peer.createAuction("Zombiecide", (double) 80, "Cardboard Game","A famous cardboard game", data ));
            assertEquals( "The auction Zombiecide is up untill " + data + " and you are winning it bidding 90.0", peer_1.placeABid("Zombiecide", (double) 90));
            assertEquals( "Not enough to win" , peer_2.placeABid("Zombiecide", (double) 70));
            assertEquals( "The auction Zombiecide is up untill " + data + " and you are winning it bidding 95.0" , peer_3.placeABid("Zombiecide", (double) 95));
            assertEquals( "The auction creator can't bid" , master_peer.placeABid("Zombiecide", (double) 100 ));


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


            Date data3 = new Date(Calendar.getInstance().getTimeInMillis() + 2000 );

            master_peer.createAuction("Cats", (double) 30, "Comics", "A famous manga written and drawn by Junji Ito",data3);
            peer_1.createAuction("Batman Hush", (double) 20, "Comics", "A Batman comic book", new Date(Calendar.getInstance().getTimeInMillis() + 1000 ) );
            peer_2.createAuction("Red Dead Redemption 2", (double) 50, "Videogames","A videogame published by Rockstar", data );
            peer_2.createAuction("GameBoy Advance", (double) 50, "Videogames","One of the first portable console made by Nintendo",data);

            Auction element = new Auction(0, "Cats","Comics" , "A famous manga written and drawn by Junji Ito", (double) 30 , data3);
            Auction element2 = new Auction(1,"Batman Hush", "Comics","A Batman comic book",(double) 20, new Date(Calendar.getInstance().getTimeInMillis() + 1000));
            Auction element3 = new Auction(2, "Red Dead Redemption 2", "Videogames","A videogame published by Rockstar", (double) 50, data);
            Auction element4 = new Auction(2, "GameBoy Advance", "Videogames","One of the first portable console made by Nintendo", (double) 50, data);
            Auction element5 = new Auction(0,"Zombiecide","Cardboard Game","A famous cardboard game", (double) 80, data);

            ArrayList<Auction> list2 = new ArrayList<Auction>();
            list2.add(element);
            list2.add(element2);
            list2.add(element3);
            list2.add(element4);

            assertEquals("The auction has no participant right now, starting with a price of 30.0 € and is up untill " + data3 ,peer_3.checkAuction("Cats"));
            assertEquals("The auction Cats is up untill " + data3 + " and you are winning it bidding 40.0" , peer_3.placeABid("Cats", (double) 40));
            assertEquals("the auction is active until " + data3 + "and right now you made the highest offer bidding 40.0",peer_3.checkAuction("Cats"));
            assertEquals("the auction is active until " + data3 + "and right now the highest offer is 40.0",peer_2.checkAuction("Cats"));

            Thread.sleep(2000);

            assertEquals("You can't bid, the auction has been won by participant 3", peer_2.placeABid("Cats", (double) 70));
            assertEquals("It is not possible for you to bid since the auction is ended with no winner", peer_2.placeABid("Batman Hush" ,(double) 30));
            assertEquals("The auction had no winner", peer_1.checkAuction("Batman Hush"));
            assertEquals("You won the auction Cats bidding 40.0 and paying 30.0", peer_3.checkAuction("Cats") );
            assertEquals("The auction winner is 3 bidding 40.0 and paying 30.0",peer_2.checkAuction("Cats"));

            ArrayList<String> result = new ArrayList<String>();
            result.add(element3.getName());
            result.add(element4.getName());

            ArrayList<Auction> list = new ArrayList<Auction>();
            list.add(element3);
            list.add(element4);

            Thread.sleep(2000);

            assertEquals(list.toString(), peer_1.findAuctionByCategory("Videogames").toString());
            assertEquals(result,peer_2.auctionOwner());

            //Third Step:
            //Peer_1 checks auction of a certain price
            //Some of them try to remove, auctions not of their own
            //Master removes its own auction
            //To prove it is correct, peer_2 performs a check of all the auctions again

            Thread.sleep(2000);

            ArrayList<Auction> all = new ArrayList<Auction>();
            ArrayList<Auction> price = new ArrayList<Auction>();

            Thread.sleep(2000);

            price.add(element3);
            price.add(element4);

            all.add(element3);
            all.add(element4);


            Thread.sleep(2000);

            assertEquals(price.toString(), peer_1.findAuctionByPrice((double) 50).toString());

            assertFalse(peer_3.removeAnAuction("GameBoy Advance"));
            assertFalse(peer_1.removeAnAuction("Red Dead Redemption 2"));

            Thread.sleep(2000);

            assertTrue(master_peer.removeAnAuction("Zombiecide"));

            Thread.sleep(4000);

            System.out.println("Eseguo finding by price");

            assertEquals(all.toString(),peer_2.checkAllAuction().toString());


        } catch (Exception e) {
            e.printStackTrace();
        }

    }



}//class
