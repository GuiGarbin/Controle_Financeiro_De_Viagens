package org.example.trip;

import org.example.trip.expenses.Expenses;
import org.example.trip.expenses.ExpensesController;
import org.example.trip.expenses.TuristicPoint;
import org.example.users.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TripController {
    public List<Trips> tripsList = new ArrayList<>();
    Trips trip;
    ExpensesController controller;

    public void TripController(){
        criarDadosFalsos();
        testeDeSaida(5);
    }


    //classe teste para se a saida esta como deveria
    private void testeDeSaida(double gasto){
        System.out.println(trip.getBudget());
        Expenses expenses = new Expenses(trip.getId(), "a", 5, "yen", "nada");
        addExpensive(expenses);
        System.out.println(controller.convertCurrency(gasto));

        System.out.println(trip.getTuristicPoint(0).getCost() + " " + trip.getTuristicPoint(0).getName());
        System.out.println(trip.getConvertedBudget());

        for(int i=0;i<trip.getDailyBudgetList().size();i++){
            System.out.println(trip.getDailyBudgetList().get(i).getDate());
            System.out.println(trip.getDailyBudgetList().get(i).getBudget() + " " + trip.getDailyBudgetList().get(i).getConvertedBudget());
        }
    }

    private void addExpensive(Expenses expenses){
        Trips trips = tripsList.getFirst();
        ExpensesController controller = new ExpensesController(trips);
        controller.addExpensive(expenses);
    }

    //classe para criar dados falsos para testes
    private void criarDadosFalsos(){
        Trips trips = new Trips(
                "japao",
                2000,
                "nada",
                "japao",
                "yen",
                LocalDate.parse("2026-04-20"),
                LocalDate.parse("2026-04-24"),
                new User("gui")
        );

        TuristicPoint turisticPoint = new TuristicPoint(135, "tah mahal");
        trips.addTuristicPoint(turisticPoint);

        tripsList.add(trips);
        trip = tripsList.get(0);
        controller = new ExpensesController(trip);
    }
}
