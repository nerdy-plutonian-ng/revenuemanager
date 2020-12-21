package com.pluto.persolrevenuemanager;

import java.util.Date;

public class Transaction {

    private String id;
    private int itemID;
    private double amount;
    private String agentID;
    private String payerID;
    private String gcrNo;
    private String location;
    private int paymentMode;
    private Date date;

    public Transaction(String id, int itemID, double amount, String agentID, String payerID, String gcrNo, String location, int paymentMode, Date date) {
        this.id = id;
        this.itemID = itemID;
        this.amount = amount;
        this.agentID = agentID;
        this.payerID = payerID;
        this.gcrNo = gcrNo;
        this.location = location;
        this.paymentMode = paymentMode;
        this.date = date;
    }

    public Transaction(String id, int itemID, double amount, String payerID, Date date) {
        this.id = id;
        this.itemID = itemID;
        this.amount = amount;
        this.payerID = payerID;
        this.date = date;
        this.agentID = "0";
        this.gcrNo = "0";
        this.location = "0.00,0.00";
        this.paymentMode = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAgentID() {
        return agentID;
    }

    public void setAgentID(String agentID) {
        this.agentID = agentID;
    }

    public String getPayerID() {
        return payerID;
    }

    public void setPayerID(String payerID) {
        this.payerID = payerID;
    }

    public String getGcrNo() {
        return gcrNo;
    }

    public void setGcrNo(String gcrNo) {
        this.gcrNo = gcrNo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(int paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
