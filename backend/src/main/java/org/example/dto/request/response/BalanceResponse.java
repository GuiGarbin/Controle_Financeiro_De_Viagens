package org.example.dto.response;

public class BalanceResponse { // DTO para mostrar o saldo líquido de um usuário em uma viagem, indicando se ele tem a receber ou deve pagar
    private String userId;
    private String userName;
    private double netBalance; // positivo = tem a receber, negativo = deve

    public BalanceResponse(String userId, String userName, double netBalance) {
        this.userId = userId;
        this.userName = userName;
        this.netBalance = netBalance;
    }

    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public double getNetBalance() { return netBalance; }
}