package it.unisa.implementation;

import it.unisa.implementation.AuctionMechanismImpl;
import it.unisa.implementation.MessageListener;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuctionMechanismImplTest {

    private static AuctionMechanismImpl master_peer;
    private static AuctionMechanismImpl peer_1;
    private static AuctionMechanismImpl peer_2;
    private static AuctionMechanismImpl peer_3;
    private static AuctionMechanismImpl peer_4;


    public AuctionMechanismImplTest() throws Exception {

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
        master_peer.leaveNetwork();
        peer_1.leaveNetwork();
        peer_2.leaveNetwork();
        peer_3.leaveNetwork();
        peer_4.leaveNetwork();
        }


    /*TEST : createAuction()
    * @result create a new auction
    * */

    @Test
    public void K_creation() throws IOException, ClassNotFoundException {
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

    /*TEST : createAuction()
     * @result creating a new auction doesn't work due to a wrong date insertion
     * */

    @Test
    public void L_creationNoWorkDate(){
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

    /*TEST : createAuction()
     * @result creating a new auction doesn't work since the auction of that product as already been created
     * */

    @Test
    public void M_creationNoWorkOtherwise(){
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


    /*TEST : checkAuction()
     * @result the check of the auction reveals that it is ended without a winner
     * */

    @Test
    public void N_checkNoWinner(){
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


    /*TEST : checkAuction()
     * @result the auction checked has no participants
     * */

    @Test
    public void O_checkNoParticipants(){
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



    /*TEST : checkAuction()
     * @result the check fails since it looks for a different product
     * */

    @Test
    public void P_checkFailure(){
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



    /*TEST : auctionOwner()
     * @result multiple peers creates multiple auctions and some of them wants to check what they own
     * */

    @Test
    public void T_whatPeersOwn(){
        System.out.println("whatPeersOwn is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            peer_4.createAuction("AGV Legend",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            peer_4.createAuction("Hallucinations", (double) 50, "Comics", "A famous manga written and drawn by Junji Ito",data);
            peer_1.createAuction("Trust Gaming GXT 488", (double) 40, "Headphones","Gaming headphones made by Trust", data);

            Thread.sleep(7000);

            Auction element = new Auction(4, "AGV Legend", "Helmets","Motorcycle equipment made by AGV",(double) 150, data);
            Auction element1 = new Auction(4, "Hallucinations","Comics" , "A famous manga written and drawn by Junji Ito", (double) 50 , data);

            ArrayList<String> list = new ArrayList<String>();

            list.add(element.getName());
            list.add(element1.getName());

            assertEquals(list, peer_4.auctionOwner());


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }



    /*TEST : findAuctionByCategory()
     * @result multiple peers creates multiple auctions and some of them wants to check what belongs to a certain category
     * */

    @Test
    public void Q_findingByCategory(){
        System.out.println("findingByCategory is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            master_peer.createAuction("AGV AX8",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            master_peer.createAuction("The Face Burglar", (double) 50, "Comics", "A famous manga written and drawn by Junji Ito",data);
            peer_1.createAuction("Logitech G533", (double) 150, "Headphones","Bluetooth headphones made by Logitech", data);
            peer_1.createAuction("Razer Kraken", (double) 120, "Headphones","Bluetooth headphones made by Razer", data );

            Thread.sleep(7000);

            Auction element = new Auction(0, "AGV AX8", "Helmets","Motorcycle equipment made by AGV",(double) 150, data);
            Auction element1 = new Auction(0, "The Face Burglar","Comics" , "A famous manga written and drawn by Junji Ito", (double) 50 , data);
            Auction element3 = new Auction (1, "Logitech G533", "Headphones","Bluetooth headphones made by Logitech",(double) 150, data);
            Auction element4 =new Auction(1,"Razer Kraken","Headphones", "Bluetooth headphones made by Razer", (double) 120 ,data);

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




    /*TEST : findAuctionByPrice()
     * @result multiple peers creates multiple auctions and some of them wants to check which of them have a certain price
     * */

    @Test
    public void U_findingByPrice(){
        System.out.println("findingByPrice is running...");
        try {

            Date data = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            data = formatter.parse("12/10/2050");
            data.setHours(11);
            data.setMinutes(30);

            master_peer.createAuction("Slug Girl", (double) 50, "Comics", "A famous manga written and drawn by Junji Ito",data);
            peer_1.createAuction("Spiderman PS4", (double) 15, "Videogames","A 2018 PS4 game", data);
            peer_2.createAuction("Dannato", (double) 15, "Comics", "A Batman comic book", data );

            Thread.sleep(3000);

            Auction element3 = new Auction (1, "Spiderman PS4", "Videogames","A 2018 PS4 game",(double) 15, data);

            ArrayList<Auction> list = new ArrayList<Auction>();
            list.add(element3);

            Thread.sleep(2000);

            assertEquals(list.toString(),peer_3.findAuctionByPrice((double) 15).toString());


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }



    /*TEST : removeAnAuction()
     * @result multiple peers creates multiple auctions and some of them at a certain point, decides to remove what belongs to its
     * */

    @Test
    public void R_removingProcess(){
        System.out.println("removingProcess is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 4000);

            master_peer.createAuction("AGV K3 SV",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            master_peer.createAuction("Frankenstein", (double) 50, "Comics", "A famous manga written and drawn by Junji Ito",data);
            peer_1.createAuction("JBL Tune", (double) 20, "Headphones","Bluetooth headphones made by JBL", data);
            peer_1.createAuction("Logitech G432", (double) 120, "Headphones","Headset made by Logitech", data );
            peer_2.createAuction("Batman Knightfall", (double) 20, "Comics", "A Batman comic book", data );

            Thread.sleep(7000);

            assertTrue(master_peer.removeAnAuction("AGV K3 SV"));
            assertTrue(peer_1.removeAnAuction("Logitech G432"));
            assertTrue(peer_1.removeAnAuction("JBL Tune"));


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }

    /*TEST : removingProcessWrong()
     * @result multiple peers creates multiple auctions and some of them tries to remove someone else's creation
     * */

    @Test
    public void S_removingProcessWrong(){
        System.out.println("removingProcessWrong is running...");
        try {

            Date data = new Date(Calendar.getInstance().getTimeInMillis() + 4000);

            master_peer.createAuction("AGV AX9",(double) 150, "Helmets","Motorcycle equipment made by AGV", data );
            master_peer.createAuction("The Bully", (double) 50, "Comics", "A famous manga written and drawn by Junji Ito",data);
            peer_1.createAuction("Sony Wh-Xb900N", (double) 20, "Headphones","Bluetooth headphones made by Sony", data);
            peer_1.createAuction("Logitech G Pro", (double) 120, "Headphones","Bluetooth headphones made by Logitech", data );
            peer_2.createAuction("Gotham by Gaslight", (double) 20, "Comics", "A Batman comic book", data );

            Thread.sleep(7000);

            assertFalse(master_peer.removeAnAuction("Gotham by Gaslight"));
            assertFalse(peer_2.removeAnAuction("Logitech G Pro"));
            assertFalse(peer_3.removeAnAuction("Sony Wh-Xb900N"));


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("----END----");
    }



}
