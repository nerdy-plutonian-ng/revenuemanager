package com.pluto.persolrevenuemanager;

public class Agent {

    private String id;
    private String firstName;
    private String lastName;
    private double cashLimit;
    private double collectedToDate;
    private double settledToDate;
    private int pin;
    private String token;
    private String assemblyName;
    private String assemblyLogo;
    private String tokenExpiry;

    public Agent() {
    }

    public Agent(String id, String firstName, String lastName, double cashLimit,
                 double collectedToDate, double settledToDate, int pin, String token,
                 String assemblyName, String assemblyLogo, String tokenExpiry) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.cashLimit = cashLimit;
        this.collectedToDate = collectedToDate;
        this.settledToDate = settledToDate;
        this.pin = pin;
        this.token = token;
        this.assemblyName = assemblyName;
        this.assemblyLogo = assemblyLogo;
        this.tokenExpiry = tokenExpiry;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public double getCashLimit() {
        return cashLimit;
    }

    public void setCashLimit(double cashLimit) {
        this.cashLimit = cashLimit;
    }

    public double getCollectedToDate() {
        return collectedToDate;
    }

    public void setCollectedToDate(double collectedToDate) {
        this.collectedToDate = collectedToDate;
    }

    public double getSettledToDate() {
        return settledToDate;
    }

    public void setSettledToDate(double settledToDate) {
        this.settledToDate = settledToDate;
    }

    public int getPin() {
        return pin;
    }

    public void setPin(int pin) {
        this.pin = pin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAssemblyName() {
        return assemblyName;
    }

    public void setAssemblyName(String assemblyName) {
        this.assemblyName = assemblyName;
    }

    public String getAssemblyLogo() {
        return assemblyLogo;
    }

    public void setAssemblyLogo(String assemblyLogo) {
        this.assemblyLogo = assemblyLogo;
    }

    public String getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(String tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }
}
