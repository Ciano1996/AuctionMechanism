package it.unisa.implementation;

import net.tomp2p.peers.PeerAddress;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;

public class Auction implements Serializable {

    Date stop_time;

    int owner;
    String name;
    String category;
    String description;
    Double start_price;

    int id_bid;
    Double winBid;
    Double secondBid;

    PeerAddress peerAddress_bid;
    PeerAddress peerAddress_oldBid;

    HashSet<PeerAddress> users;

    public Auction(){}

    //Constructor

    public Auction(int creator, String auctionName,String cat , String descript, Double price , Date end){
        owner =creator;
        name =auctionName;
        category = cat;
        description = descript;
        start_price =price;
        stop_time = end;
        users= new HashSet<PeerAddress>();
        id_bid = owner;
        winBid = price;
    }


    //Getter and Setter


    public Date getStop_time() {
        return stop_time;
    }

    public void setStop_time(Date stop_time) {
        this.stop_time = stop_time;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getStart_price() {
        return start_price;
    }

    public void setStart_price(Double start_price) {
        this.start_price = start_price;
    }

    public int getId_bid() {
        return id_bid;
    }

    public void setId_bid(int id_bid) {
        this.id_bid = id_bid;
    }

    public Double getWinBid() {
        return winBid;
    }

    public void setWinBid(Double winBid) {
        this.winBid = winBid;
    }

    public Double getSecondBid() {
        return secondBid;
    }

    public void setSecondBid(Double secondBid) {
        this.secondBid = secondBid;
    }

    public PeerAddress getPeerAddress_bid() {
        return peerAddress_bid;
    }

    public void setPeerAddress_bid(PeerAddress peerAddress_bid) {
        this.peerAddress_bid = peerAddress_bid;
    }

    public PeerAddress getPeerAddress_oldBid() {
        return peerAddress_oldBid;
    }

    public void setPeerAddress_oldBid(PeerAddress peerAddress_oldBid) {
        this.peerAddress_oldBid = peerAddress_oldBid;
    }

    public HashSet<PeerAddress> getUsers() {
        return users;
    }

    public void setUsers(HashSet<PeerAddress> users) {
        this.users = users;
    }

    @Override
    public String toString() {
        return
                "\n____________________________" +
                        "\n- Name = " + name +
                        "\n- Deadline = " + stop_time +
                        "\n- Owner = " + owner +
                        "\n- Category = " + category +
                        "\n- Description = "+ description +
                        "\n____________________________";
    }
}
