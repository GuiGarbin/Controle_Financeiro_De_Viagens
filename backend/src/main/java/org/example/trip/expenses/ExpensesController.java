package org.example.trip.expenses;

import org.example.trip.Trips;
import org.example.trip.daily.DailyBudget;

import java.util.ArrayList;
import java.util.List;

public class ExpensesController {
    Trips trips;
    List<DailyBudget> dailyBudgetList = new ArrayList<>();

    public ExpensesController(Trips trips) {
        this.trips = trips;
        this.dailyBudgetList = trips.getDailyBudgetList();
    }

}
