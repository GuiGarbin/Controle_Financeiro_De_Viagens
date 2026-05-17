package org.example.dto.response;

import org.example.model.Expense;
import java.util.List;

public class ExpenseBreakdownResponse { // DTO para mostrar o detalhamento de uma despesa, incluindo quem pagou, quanto cada um deve e como foi dividido
    private String expenseId;
    private String description;
    private double totalAmount;
    private String paidByUserId;
    private List<Expense.Split> splits;

    public ExpenseBreakdownResponse(String expenseId, String description,
                                     double totalAmount, String paidByUserId,
                                     List<Expense.Split> splits) {
        this.expenseId = expenseId;
        this.description = description;
        this.totalAmount = totalAmount;
        this.paidByUserId = paidByUserId;
        this.splits = splits;
    }

    public String getExpenseId() { return expenseId; }
    public String getDescription() { return description; }
    public double getTotalAmount() { return totalAmount; }
    public String getPaidByUserId() { return paidByUserId; }
    public List<Expense.Split> getSplits() { return splits; }
}