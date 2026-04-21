package org.example.trip.expenses;

import java.util.Date;

public class Expenses {
    private int id;
    private int tripId;
    private String description;
    private double amount;
    private double currencyValue;
    private String currency;
    private Date date;
    private String notes;
    private Date createdAt;
    ExpensesController controller;

    public Expenses(int tripId,
                    String description,
                    double amount,
                    double currencyValue,
                    String currency,
                    String notes) {
        this.tripId = tripId;
        this.description = description;
        this.amount = amount;
        this.currencyValue = currencyValue;
        this.currency = currency;
        this.date = new Date();
        this.notes = notes;
        this.createdAt = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public double getConvertedValue(){
        return this.amount*this.currencyValue;
    }

    public double getCurrencyValue() {
        return currencyValue;
    }

    public void setCurrencyValue(double currencyValue) {
        this.currencyValue = currencyValue;
    }
}
