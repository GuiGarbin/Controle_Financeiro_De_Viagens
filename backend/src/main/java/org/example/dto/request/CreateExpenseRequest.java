package org.example.dto.request;

import org.example.model.Expense; // importa a classe  para usar a classe interna Split
import org.example.model.enums.SplitMethod; // importa a enum SplitMethod para usar no campo splitMethod
import java.util.List; // importa a classe List para usar no campo splits

public class CreateExpenseRequest { 
    private String tripId;
    private String description;
    private double amount;
    private String currency;
    private String categoryId;
    private String paidByUserId;
    private SplitMethod splitMethod;
    private List<Expense.Split> splits;
    private String date;
    private String notes;

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

    public List<Expense.Split> getSplits() { return splits; }
    public void setSplits(List<Expense.Split> splits) { this.splits = splits; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}