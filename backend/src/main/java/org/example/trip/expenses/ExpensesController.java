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

    public double convertCurrency(Expenses expenses) {
        String currencyExpense = expenses.getCurrency();
        String currencyTrip = trips.getCurrency();
        if(currencyTrip.equalsIgnoreCase(currencyExpense)){
            return expenses.getAmount();
        }
        return expenses.getAmount() * (expenses.getCurrencyValue() / trips.getCurrencyValue());
    }

    public void addExpensiveDay(Expenses expenses, int day){
        DailyBudget a = dailyBudgetList.get(day);
        if(expenses.getCurrency().equalsIgnoreCase(trips.getCurrency())){
            double inverted = 1 / expenses.getCurrencyValue();
            double expenseConverted = expenses.getAmount() * inverted;
            expenses.setAmount(expenseConverted);
        }
        a.addExpense(expenses);
    }

    public double verifyBudgetTrip(){
        double budget=trips.getInitialBudget();
        for(int i=0;i<trips.getDailyBudgetList().size();i++){
            if(trips.getDailyBudgetList().get(i).getListExpenses()!=null){
                for(int j=0;j<trips.getDailyBudgetList().get(i).getListExpenses().size();j++){
                    budget-=trips.getDailyBudgetList().get(i).getListExpenses().get(j).getAmount();
                }
            }
        }
        return budget;
    }


    public double verifyBudgetDay(int day){
        DailyBudget a = dailyBudgetList.get(day);
        double budget=a.getBudget();
        for(int i=0;i<a.getListExpenses().size();i++){
            budget-=a.getListExpenses().get(i).getAmount();
        }
        return budget;
    }

}
