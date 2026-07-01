package org.example.dto.request.response;

import org.example.expenses.Expenses;

// Resposta da criacao de um gasto. Alem do gasto criado, devolve a verificacao
// de orcamento (acima do limite / perto do limite) que antes so era impressa no
// console (System.out.println), conforme docs/APIintegration.md secao 2.2.
public class AddExpenseResponse {
    private Expenses expense;
    private double dailyBudgetRemaining; // saldo restante do dia (moeda local)
    private double tripBudgetRemaining;  // saldo restante da viagem (moeda local)
    private boolean overBudget;          // estourou o orcamento do dia
    private boolean nearLimit;           // dentro dos ultimos 10% do orcamento do dia
    private String warning;              // mensagem amigavel (pode ser null)

    public AddExpenseResponse() {}

    public Expenses getExpense() { return expense; }
    public void setExpense(Expenses expense) { this.expense = expense; }

    public double getDailyBudgetRemaining() { return dailyBudgetRemaining; }
    public void setDailyBudgetRemaining(double dailyBudgetRemaining) { this.dailyBudgetRemaining = dailyBudgetRemaining; }

    public double getTripBudgetRemaining() { return tripBudgetRemaining; }
    public void setTripBudgetRemaining(double tripBudgetRemaining) { this.tripBudgetRemaining = tripBudgetRemaining; }

    public boolean isOverBudget() { return overBudget; }
    public void setOverBudget(boolean overBudget) { this.overBudget = overBudget; }

    public boolean isNearLimit() { return nearLimit; }
    public void setNearLimit(boolean nearLimit) { this.nearLimit = nearLimit; }

    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }
}
