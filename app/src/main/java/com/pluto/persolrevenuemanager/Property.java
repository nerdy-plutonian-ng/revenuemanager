package com.pluto.persolrevenuemanager;

public class Property {

    private String accountCode;
    private String billId;
    private String name;
    private String type;
    private double currentCharge;
    private double outBalance;
    private double paid;

    public Property(String accountCode, String billId, String name, String type, double currentCharge, double outBalance, double paid) {
        this.accountCode = accountCode;
        this.billId = billId;
        this.name = name;
        this.type = type;
        this.currentCharge = currentCharge;
        this.outBalance = outBalance;
        this.paid = paid;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getCurrentCharge() {
        return currentCharge;
    }

    public void setCurrentCharge(double currentCharge) {
        this.currentCharge = currentCharge;
    }

    public double getOutBalance() {
        return outBalance;
    }

    public void setOutBalance(double outBalance) {
        this.outBalance = outBalance;
    }

    public double getPaid() {
        return paid;
    }

    public void setPaid(double paid) {
        this.paid = paid;
    }
}