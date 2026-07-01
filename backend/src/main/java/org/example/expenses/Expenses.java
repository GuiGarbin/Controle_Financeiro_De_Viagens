package org.example.expenses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.example.util.IdGenerator;

import java.time.LocalDate;

public class Expenses {
    private String id;
    private String tripId;
    private String description;
    private double amount;
    private double currencyValue;
    private String currency;
    // Datas padronizadas em LocalDate (formato dd/MM/yyyy no JSON), em linha com o
    // restante do modelo. Antes era java.util.Date, o que gerava serializacao inconsistente.
    private LocalDate date;
    private String notes;
    private LocalDate createdAt;

    public Expenses(){

    }

    public Expenses(String tripId,
                    String description,
                    double amount,
                    double currencyValue,
                    String currency,
                    String notes) {
        this.id = IdGenerator.forExpense();
        this.tripId = tripId;
        this.description = description;
        this.amount = amount;
        this.currencyValue = currencyValue;
        this.currency = currency;
        this.date = LocalDate.now();
        this.notes = notes;
        this.createdAt = LocalDate.now();
    }

    @JsonIgnore
    public double getConvertedAmount(){
        return this.amount * currencyValue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @JsonIgnore
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
