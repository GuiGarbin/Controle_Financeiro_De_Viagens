package org.example.dto.request.response;

import java.util.List;

public class TripSummaryResponse { // DTO para mostrar um resumo da viagem, incluindo o total gasto, número de despesas e saldo de cada usuário
    private String tripId;
    private String tripName;
    private double totalAmount;
    private int totalExpenses;
    private List<BalanceResponse> balances;

    public TripSummaryResponse(String tripId, String tripName, double totalAmount,
                                int totalExpenses, List<BalanceResponse> balances) {
        this.tripId = tripId;
        this.tripName = tripName;
        this.totalAmount = totalAmount;
        this.totalExpenses = totalExpenses;
        this.balances = balances;
    }

    public String getTripId() { return tripId; }
    public String getTripName() { return tripName; }
    public double getTotalAmount() { return totalAmount; }
    public int getTotalExpenses() { return totalExpenses; }
    public List<BalanceResponse> getBalances() { return balances; }
}