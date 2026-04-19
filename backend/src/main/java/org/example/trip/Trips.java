package org.example.trip;

import org.example.trip.daily.DailyBudget;
import org.example.trip.expenses.Expenses;
import org.example.trip.expenses.ExpensesController;
import org.example.trip.expenses.TuristicPoint;
import org.example.users.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Trips {
    private int id=0;
    private String name;
    private double budget;
    private double convertedBudget;
    private String description;
    private String destination;
    private String currency;
    private boolean status;
    private List<DailyBudget> dailyBudgetList = new ArrayList<>();
    private List<TuristicPoint> listTuristic = new ArrayList<>();
    private LocalDate startDate;
    private LocalDate endDate;
    private User createdBy;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private ExpensesController controller;

    public Trips(String name,
                 double budget,
                 String description,
                 String destination,
                 String currency,
                 LocalDate startDate, LocalDate endDate,
                 User createdBy) {
        this.id = this.id++;
        this.name = name;
        this.description = description;
        this.destination = destination;
        this.currency = currency;
        this.budget = budget;
        this.status = true;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        controller = new ExpensesController(this);
        this.convertedBudget = controller.convertCurrency(budget);
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double dailyBudget = budget / totalDays;
        double convertedDailyBudget = controller.convertCurrency(dailyBudget);
        for(int i=0;i<totalDays;i++){
            dailyBudgetList.add(new DailyBudget(startDate.plusDays(i), dailyBudget, convertedDailyBudget, null));
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getCurrency() {
        if (currency.equalsIgnoreCase("yen")) return 31.86;
        else if (currency.equalsIgnoreCase("dolar")) return 0.20;
        else if (currency.equalsIgnoreCase("euro")) return 0.17;
        else return 0;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }


    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<TuristicPoint> getListTuristic() {
        return listTuristic;
    }

    public void setListTuristic(List<TuristicPoint> listTuristic) {
        this.listTuristic = listTuristic;
    }

    public void addTuristicPoint(TuristicPoint turisticPoint){
        listTuristic.add(turisticPoint);
    }

    public TuristicPoint getTuristicPoint(int index){
        return listTuristic.get(index);
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getConvertedBudget() {
        return convertedBudget;
    }

    public void setConvertedBudget(double convertedBudget) {
        this.convertedBudget = convertedBudget;
    }

    public List<DailyBudget> getDailyBudgetList() {
        return dailyBudgetList;
    }

    public void setDailyBudgetList(List<DailyBudget> dailyBudgetList) {
        this.dailyBudgetList = dailyBudgetList;
    }
}
