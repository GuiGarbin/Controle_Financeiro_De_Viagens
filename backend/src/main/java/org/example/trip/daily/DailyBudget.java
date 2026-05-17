package org.example.trip.daily;

import org.example.trip.Trips;
import org.example.trip.expenses.Expenses;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyBudget {
    private LocalDate date;
    private double budget;
    private List<Expenses> listExpenses;

    public DailyBudget(LocalDate date, double budget) {
        this.date = date;
        this.budget = budget;
        this.listExpenses = new ArrayList<>();
    }

    public double totalExpense(){
        double total = 0;
        for(Expenses expenses : listExpenses){
            total += expenses.getAmount();
        }
        return total;
    }

    public double verifyBudget(){
        double remain = this.budget;
        for(Expenses expenses : listExpenses){
            remain -= expenses.getAmount();
        }
        return remain;
    }

    public double verifyBudgetReal(double currency){
        return verifyBudget() * ( 1 / currency);
    }

    public Expenses getExpense(int index){
        return listExpenses.get(index);
    }

    public void addExpense(Expenses expenses, Trips trip){
        this.listExpenses.add(expenses);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getBudgetReal(double currency){
        return budget * (1/currency);
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public List<Expenses> getListExpenses() {
        return listExpenses;
    }

    public void setListExpenses(List<Expenses> listExpenses) {
        this.listExpenses = listExpenses;
    }
}
