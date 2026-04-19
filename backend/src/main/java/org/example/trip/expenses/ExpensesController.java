package org.example.trip.expenses;

import org.example.trip.Trips;

public class ExpensesController {
    Trips trips;

    public ExpensesController(Trips trips) {
        this.trips = trips;
    }

    public double convertExpense(double value) {
        return value*= trips.getCurrency();
    }
}
