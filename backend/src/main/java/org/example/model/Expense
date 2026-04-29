package org.example.model;

import org.example.model.enums.SplitMethod;
import java.util.List;

public class Expense extends BaseEntity {
    private String tripId;
    private String description;
    private double amount;
    private String currency;
    private String categoryId;
    private String paidByUserId;
    private SplitMethod splitMethod;
    private List<Split> splits;
    private String date;
    private String notes;

    // Classe interna que representa a divisão por pessoa
    public static class Split {
        private String userId;
        private double amount;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getPaidByUserId() { return paidByUserId; }
    public void setPaidByUserId(String paidByUserId) { this.paidByUserId = paidByUserId; }

    public SplitMethod getSplitMethod() { return splitMethod; }
    public void setSplitMethod(SplitMethod splitMethod) { this.splitMethod = splitMethod; }

    public List<Split> getSplits() { return splits; }
    public void setSplits(List<Split> splits) { this.splits = splits; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}