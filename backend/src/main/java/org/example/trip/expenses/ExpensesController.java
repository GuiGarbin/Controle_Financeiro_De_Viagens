package org.example.trip.expenses;

import org.example.trip.Trip;
import org.example.trip.daily.DailyBudget;
import org.example.dto.request.response.ApiResponse;

import java.util.ArrayList;
import java.util.List;

public class ExpensesController {
    Trip trip;
    List<DailyBudget> dailyBudgetList = new ArrayList<>();

    public ExpensesController(Trip trip) {
        this.trip = trip;
        this.dailyBudgetList = trip.getDailyBudgetList();
    }

}
