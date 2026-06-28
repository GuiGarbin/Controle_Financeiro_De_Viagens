package org.example.dto.request;

// DTO de entrada para criar uma viagem via REST (POST /api/trips).
// Espelha exatamente o corpo descrito em docs/APIintegration.md (secao 2.1).
// As datas chegam como String no formato dd/MM/yyyy e sao convertidas no controller.
public class TripCreateRequest {
    private String name;
    private double budget;
    private String description;
    private String destination;
    private String currency;
    private String startDate; // dd/MM/yyyy
    private String endDate;   // dd/MM/yyyy

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
