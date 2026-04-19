package org.example.trip.expenses;

import org.example.trip.Trips;

public class ExpensesController {
    Trips trips;

    public ExpensesController(Trips trips) {
        this.trips = trips;
    }

    public double convertCurrency(double value) {
        return value*= trips.getCurrency();
    }

    public void addExpensive(Expenses expenses){
        trips.setBudget(trips.getBudget()-expenses.getAmount());
        trips.setConvertedBudget(convertCurrency(trips.getBudget()));
        
    }
}
