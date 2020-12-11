package it.unisa.implementation;

import java.io.IOException;
import java.util.Date;

public interface AuctionMechanism {

    public boolean createAuction(String auction_name, Double start_price, String category, String description, Date end_time) throws IOException, ClassNotFoundException, InterruptedException;
    public String checkAuction(String auction_name ) throws IOException, ClassNotFoundException, InterruptedException;
    public String placeABid(String auctionName, Double bid) throws IOException, ClassNotFoundException;

}
