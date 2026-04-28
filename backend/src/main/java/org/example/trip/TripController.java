package org.example.trip;

import org.example.trip.daily.DailyBudget;
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
        testeDeSaida();
        //menu();
    }

    //classe teste para se a saida esta como deveria
    private void testeDeSaida(){
        //Teste de saldo inicial
        System.out.println("Saldo inicial: " + trip.getInitialBudget());
        System.out.println("Saldo total: " + controller.verifyBudgetTrip());

        //Teste de adicao de gasto (somente diario)
        Expenses expenses = new Expenses(trip.getId(), "gasto teste", 300, trip.getCurrencyValue(), "yen", "nada");
        addExpensive(expenses, 1);
        System.out.println("Saldo restante total: " + controller.verifyBudgetTrip());
        System.out.println("Saldo restante do dia: " + controller.verifyBudgetDay(0));

        //Teste de conversao cambial
        System.out.println("Converter gasto: " + controller.convertCurrency(expenses));

        System.out.println(trip.getTuristicPoint(0).getCost() + " " + trip.getTuristicPoint(0).getName());

        for(int i=0;i<trip.getDailyBudgetList().size();i++){
            System.out.println(trip.getDailyBudgetList().get(i).getDate());
            System.out.println(controller.verifyBudgetDay(i) + " " + trip.getDailyBudgetList().get(i).getConvertedBudget());
        }
        System.out.println(controller.verifyBudgetDay(1));
    }

    private void addExpensive(Expenses expenses, int day){
        Trips trips = tripsList.getFirst();
        ExpensesController controller = new ExpensesController(trips);
        controller.addExpensiveDay(expenses, day-1);
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

    private void menu(){
        boolean on = true;
        while (on){
            Scanner scanner = new Scanner(System.in);
            System.out.println("=====================");
            System.out.println("Orcamento total: " + controller.verifyBudgetTrip());
            System.out.println("Orcamento total convertido: " + trip.getConvertedBudget());
            System.out.println("Conferir dia: ");
            for (int i=0;i<trip.getDailyBudgetList().size();i++){
                System.out.println("Dia " + i + ": " + trip.getDailyBudgetList().get(i).getDate());
                System.out.println("Orcamento: " + trip.getDailyBudgetList().get(i).getBudget());
                System.out.println("Orcamento convertido: " + trip.getDailyBudgetList().get(i).getConvertedBudget());
            }
            System.out.println("Selecione o dia: ");
            int day = scanner.nextInt();
            System.out.println("1- registrar gasto");
            System.out.println("0 - sair");
            int opcao = scanner.nextInt();
            switch (opcao){
                case 1:
                    System.out.println("qual o valor do gasto?");
                    double value = scanner.nextDouble();
                    addExpensive(new Expenses(trip.getId(), null, value, trip.getCurrencyValue(), "yen", null), day);
                    break;
                case 0:
                    on=false;
            }
        }
    }
}
