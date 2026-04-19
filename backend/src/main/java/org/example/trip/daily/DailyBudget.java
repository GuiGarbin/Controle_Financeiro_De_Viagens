package org.example.trip.daily;

import org.example.trip.expenses.Expenses;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class DailyBudget {
    private LocalDate date;
    private double budget;
    private double convertedBudget;
    private List<Expenses> expenses;

    public DailyBudget(LocalDate date, double budget, double convertedBudget, List<Expenses> expenses) {
        this.date = date;
        this.budget = budget;
        this.convertedBudget = convertedBudget;
        this.expenses = expenses;
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

    public List<Expenses> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<Expenses> expenses) {
        this.expenses = expenses;
    }

    public double getConvertedBudget() {
        return convertedBudget;
    }

    public void setConvertedBudget(double convertedBudget) {
        this.convertedBudget = convertedBudget;
    }
}
