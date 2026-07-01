package org.example.dto.request;

// DTO de entrada para adicionar um gasto a uma viagem
// (POST /api/trips/{id}/expenses). A data informada (dd/MM/yyyy) define em qual
// dia da viagem o gasto entra — diferente do fluxo de terminal, que so usava "hoje".
public class AddExpenseRequest {
    private String description;
    private double amount;
    private String date;  // dd/MM/yyyy
    private String notes;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
