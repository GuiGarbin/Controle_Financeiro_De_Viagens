package org.example.trip.daily;

import org.example.trip.expenses.Expenses;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DailyBudget {
    private LocalDate date;
    private double budget;
    private double convertedBudget;
    private List<Expenses> listExpenses;

    public DailyBudget(LocalDate date, double budget, double convertedBudget) {
        this.date = date;
        this.budget = budget;
        this.convertedBudget = convertedBudget;
        this.listExpenses = new ArrayList<>();
    }

    public void addExpense(Expenses expenses){
        this.listExpenses.add(expenses);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public double getConvertedBudget() {
        return convertedBudget;
    }

    public void setConvertedBudget(double convertedBudget) {
        this.convertedBudget = convertedBudget;
    }
}
