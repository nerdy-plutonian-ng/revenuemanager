package com.pluto.persolrevenuemanager;

public class Item {

    private int itemId;
    private String name;
    private double baseAmount;

    public Item(int itemId, String name, double baseAmount) {
        this.itemId = itemId;
        this.name = name;
        this.baseAmount = baseAmount;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(double baseAmount) {
        this.baseAmount = baseAmount;
    }
}
