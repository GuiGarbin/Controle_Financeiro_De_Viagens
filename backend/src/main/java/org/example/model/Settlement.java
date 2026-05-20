package org.example.model;

public class Settlement extends BaseEntity {
    private String tripId;
    private String fromUserId;
    private String toUserId;
    private double amount;
    private String currency;
    private String note;
    private String settledAt;

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getToUserId() { return toUserId; }
    public void setToUserId(String toUserId) { this.toUserId = toUserId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getSettledAt() { return settledAt; }
    public void setSettledAt(String settledAt) { this.settledAt = settledAt; }
}