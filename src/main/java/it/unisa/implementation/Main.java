package it.unisa.implementation;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Main {

    @Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
    private static String master;

    @Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
    private static int id;

    public static void main(String[] args) throws Exception{

        class MessageListenerImpl implements MessageListener{
            int peerId;

            public MessageListenerImpl(int peerId){
                this.peerId=peerId;
            }

            @Override
            public Object parseMessage(Object msg) {
                TextIO textIO = TextIoFactory.getTextIO();
                TextTerminal terminal = textIO.getTextTerminal();
                terminal.printf("\n"+peerId+"] (Direct Message Received) "+ msg +"\n\n");
                return "success";
            }
        }

        Main example = new Main();

        final CmdLineParser parser = new CmdLineParser(example);

        try{
            parser.parseArgument(args);
            TextIO textIO = TextIoFactory.getTextIO();
            TextTerminal terminal = textIO.getTextTerminal();

            AuctionMechanismImpl implementation = new AuctionMechanismImpl(id, master, new MessageListenerImpl(id));
            terminal.printf("\nStaring peer id: %d on master node: 127.0.0.1\n", id);
            String auctionNames="";

            while(true){
                printMenu(terminal);

                //Reading user input
                int option = textIO.newIntInputReader()
                                .withMaxVal(9)
                                .withMinVal(1)
                                .read("\nOption");

                switch (option){

                    case 1:
                        terminal.printf("\nCHECK ALL AUCTIONS\n");

                        ArrayList<Auction> allAuct = implementation.checkAllAuction();
                        if (allAuct != null) {
                            for (Auction a : allAuct) {
                                terminal.printf(a.toString());
                            }
                        } else {
                            terminal.printf("\nTHERE'S NO ACTIVE AUCTION!\n");
                        }
                        break;

                    case 2:
                        terminal.printf("\nCREATE AN AUCTION\n");
                        String auction_name = textIO.newStringInputReader()
                                .read(" Auction Name: ");

                        String endDateTime = textIO.newStringInputReader()
                                .read(" Insert Day of the End of Auction (GG/MM/AAAA): ");

                        if (endDateTime.split("/").length != 3) {
                            terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                            break;
                        }

                        if (endDateTime.split("/")[0].length() > 2) {
                            terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                            break;
                        }
                        if (endDateTime.split("/")[1].length() > 2) {
                            terminal.printf("\nMONTH NOT INSERTED CORRECTLY\n");
                            break;
                        }
                        if (endDateTime.split("/")[2].length() > 4) {
                            terminal.printf("\nYEAR NOT INSERTED CORRECTLY\n");
                            break;
                        }

                        int day = 0;
                        int month = 0;
                        int year = 0;

                        try {
                            day = Integer.parseInt(endDateTime.split("/")[0]);
                            month = Integer.parseInt(endDateTime.split("/")[1]);
                            year = Integer.parseInt(endDateTime.split("/")[2]);
                        } catch (NumberFormatException e) {
                            terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                            break;
                        }


                        if (day > 31) {
                            terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                            break;
                        }
                        if (year % 4 == 0 && month == 2 && day > 29) {
                            terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                            break;
                        }
                        if ((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
                            terminal.printf("\nDATE NOT INSERTED CORRECTLY\n");
                            break;
                        }


                        String time_hours = textIO.newStringInputReader()
                                .read(" Insert the Hour of the End of Auction (hh:mm): ");

                        if (time_hours.split(":").length != 2) {
                            terminal.printf("\nHOUR NOT INSERTED CORRECTLY\n");
                            break;
                        }

                        if ((time_hours.split(":")[0].length() > 2) || (time_hours.split(":")[1].length() > 2)) {
                            terminal.printf("\nHOUR NOT INSERTED CORRECTLY\n");
                            break;
                        }

                        int hours = 0;
                        int minutes = 0;

                        try {
                            hours = Integer.parseInt(time_hours.split(":")[0]);
                            minutes = Integer.parseInt(time_hours.split(":")[1]);
                        } catch (NumberFormatException e) {
                            terminal.printf("\nHOUR NOT INSERTED CORRECTLY\n");
                            break;
                        }


                        if ((hours > 24) || (minutes > 59)) {
                            terminal.printf("\nHOUR NOT INSERTED CORRECTLY\n");
                            break;
                        }

                        String dateInString = day + "/" + month + "/" + year;
                        java.util.Date endingDate = null;

                        try {
                            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                            endingDate = formatter.parse(dateInString);
                            endingDate.setHours(hours);
                            endingDate.setMinutes(minutes);


                        } catch (ParseException e) {
                            System.out.println(e.toString());
                            e.printStackTrace();
                        }


                        String start_price = textIO.newStringInputReader()
                                .read(" Insert Reserved Price (use '.' to divide cents): ");

                        if (start_price.length() > 10) {
                            terminal.printf("\nThe Bid is too big!\n");
                            break;
                        }

                        Double starting_price = 0.0;
                        try {
                            starting_price = Double.parseDouble(start_price);

                        } catch (NumberFormatException e) {
                            terminal.printf("\nERROR OCCURED CREATING THIS AUCTION\n");
                            break;
                        }

                        String category = textIO.newStringInputReader()
                                .read(" Insert Category: ");


                        String description = textIO.newStringInputReader()
                                .read(" Insert Description: ");

                        boolean createdAuction = implementation.createAuction(auction_name, starting_price, category, description, endingDate);

                        if (createdAuction == true) {
                            terminal.printf("\nAUCTION CREATED SUCCESSFULLY\n");
                        } else {
                            terminal.printf("\nERROR OCCURED CREATING THIS AUCTION\n");
                        }
                        break;

                    case 3:
                        terminal.printf("\nCHECK AN AUCTION\n");
                        auctionNames = textIO.newStringInputReader()
                                .read(" Auction Name:");

                        String checked_Auction = implementation.checkAuction(auctionNames);

                        if (checked_Auction != null)
                            terminal.printf("\n%s\n", checked_Auction);
                        else
                            terminal.printf("\nERROR OCCURED CHECKING THIS AUCTION\n");

                        break;

                    case 4:
                        terminal.printf("\nPLACE A BID\n");
                        auctionNames = textIO.newStringInputReader()
                                .read(" Auction Name:");

                        String amount = textIO.newStringInputReader()
                                .read(" Insert a Bid Amount:");

                        Double bid = 0.0;
                        try {
                            bid = Double.parseDouble(amount);
                            //

                        } catch (NumberFormatException e) {
                            terminal.printf("\nERROR OCCURED PLACING THIS BID\n");
                            break;
                        }


                        String method = implementation.placeABid(auctionNames, bid);
                        if (method != null) {
                            terminal.printf("\n%s\n", method);
                        } else {
                            terminal.printf("\nERROR OCCURED PLACING THIS BID\n");
                        }
                        break;

                    case 5:
                        terminal.printf("\nKNOW WHAT YOU OWN\n");

                        ArrayList<String> resultingNames = implementation.auctionOwner();

                        if (!resultingNames.isEmpty()){
                            terminal.printf("YOU OWN THE AUCTIONS: \n");
                            for (String s : resultingNames ) {
                                terminal.printf(s + "\n");
                            }
                        } else
                            terminal.printf("YOU DON'T OWN ANY AUCTION");

                        break;

                    case 6:
                        terminal.printf("\nREMOVE AN AUCTION\n");
                        String name = textIO.newStringInputReader()
                                .read(" Auction Name: ");

                        boolean isSuccess = implementation.removeAnAuction(name);
                        if (isSuccess) {
                            terminal.printf("AUCTION SUCCESSFULLY REMOVED");
                        } else
                            terminal.printf("AN ERROR OCCURRED DURING THIS OPERATION");
                        break;

                    case 7:
                        terminal.printf("\nFIND AUCTION BY CATEGORY\n");
                        category = textIO.newStringInputReader()
                                .read(" Category: ");

                        ArrayList<Auction> results = implementation.findAuctionByCategory(category);
                        if (!results.isEmpty()) {
                            for (Auction a : results) {
                                terminal.printf(a.toString());
                            }
                        } else
                            terminal.printf("THERE'S NO ACTIVE AUCTION BELONGING TO THIS CATEGORY!");
                        break;

                    case 8:
                        terminal.printf("\nFIND AUCTION BY PRICE\n");
                        Double amountOM = textIO.newDoubleInputReader()
                                .read(" Amount of money you currently have: ");

                        ArrayList<Auction> resultsOM = implementation.findAuctionByPrice(amountOM);
                        if (!resultsOM.isEmpty()) {
                            for (Auction a : resultsOM) {
                                terminal.printf(a.toString());
                            }
                        } else
                            terminal.printf("THERE'S NO ACTIVE AUCTION WITH THIS AMOUNT!");
                        break;

                    case 9:
                        terminal.printf("\nEXIT\n");

                        terminal.printf("GOODBYE!!");


                        implementation.leaveNetwork();
                        System.exit(0);
                        break;

                }//switch

            }//while


        } catch (Exception e) {
            e.printStackTrace();
        }


    } //main

    public static void printMenu(TextTerminal terminal) {
        terminal.printf("\n1) - CHECK ALL THE AVAILABLE AUCTIONS -\n");
        terminal.printf("\n2) - CREATE A NEW AUCTION -\n");
        terminal.printf("\n3) - CHECK THE STATUS OF AUCTION -\n");
        terminal.printf("\n4) - PLACE A BID -\n");
        terminal.printf("\n5) - KNOW THE AUCTIONS YOU OWN -\n");
        terminal.printf("\n6) - REMOVE AN AUCTION -\n");
        terminal.printf("\n7) - FIND AUCTIONS BY CATEGORY -\n");
        terminal.printf("\n8) - FIND AUCTIONS BY PRICE -\n");
        terminal.printf("\n9) - EXIT -\n");

    }

}
