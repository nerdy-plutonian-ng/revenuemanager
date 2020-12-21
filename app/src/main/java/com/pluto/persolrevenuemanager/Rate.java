package com.pluto.persolrevenuemanager;

public class Rate {

    private int id;
    private String name;
    private double rate;
    private int itemId;

    public Rate(int id, String name, double rate, int itemId) {
        this.id = id;
        this.name = name;
        this.rate = rate;
        this.itemId = itemId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
