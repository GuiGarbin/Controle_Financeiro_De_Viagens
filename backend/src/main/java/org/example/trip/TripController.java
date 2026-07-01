package org.example.trip;

import org.example.config.JsonStorageConfig;
import org.example.repository.TripRepository;
import org.example.daily.DailyBudget;
import org.example.expenses.Expenses;
import org.example.service.CurrencyService;
import org.example.users.User;
import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TripController {
    public List<Trip> tripList = new ArrayList<>();
    TripRepository tripRepository;
    User user;
    HttpClient clienteWeb = HttpClient.newHttpClient();
    CurrencyService cambioApi = new CurrencyService();

    public TripController(){
        configJson();
        falseUser();
    }

    private void configJson(){
        JsonStorageConfig storageConfig = new JsonStorageConfig();
        tripRepository = storageConfig.tripRepository(storageConfig.objectMapper());
    }

    private void falseUser(){
        user = new User("Guilherme", LocalDate.now());
    }


    public void addExpensiveToTrip(Trip trip, String description, double amount){
        double currentCurrency;
        try {
          currentCurrency = cambioApi.getRate(trip.getCurrency(), "BRL");
        } catch (RuntimeException e) {
            currentCurrency = trip.getCurrencyValue();
        }
        Expenses expenses = new Expenses(trip.getId(), description, amount, currentCurrency, trip.getCurrency(), "");
        LocalDate today = LocalDate.now();
        for(DailyBudget d : trip.getDailyBudgetList()){
            if(d.getDate().equals(today)){
                d.addExpense(expenses);
                if(d.verifyBudgetRemaining() < 0){
                    System.out.println("Voce passou do orcamento diario");
                } else if(d.verifyBudgetRemaining() <= (d.getBudget() * 0.1) && d.verifyBudgetRemaining() >= 0){
                    System.out.println("Voce esta chegando perto do limite diario");
                }
            }
        }
        tripRepository.save(trip);
    }

    public void createTrip(String name,
                           double budget,
                           String description,
                           String country,
                           String currency,
                           LocalDate startDate,
                           LocalDate finalDate){

        double valueCurrency = cambioApi.getRate(currency, "BRL");

        Trip trip = new Trip(
                name,
                budget,
                description,
                country,
                currency,
                valueCurrency,
                startDate,
                finalDate,
                user.getId()
        );

        tripList = tripRepository.findAll();
        for(Trip t : tripList){
            if(t.isStatus()){
                t.setStatus(false);
                tripRepository.save(t);
            }
        }
        tripRepository.save(trip);
        System.out.println("Viagem criada com sucesso!");
    }

    public User getUser(){
        return user;
    }

    public List<Trip> getTripList(){
        return tripRepository.findAll();
    }

    public boolean hasTripOnGoing(){
        tripList = tripRepository.findAll();
        for(Trip t: tripList){
            if(t.isStatus()){
                return true;
            }
        }
        return false;
    }

}
