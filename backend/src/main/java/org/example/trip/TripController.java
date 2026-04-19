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

    public void TripController(){

        criarDadosFalsos();
        Trips trip = tripsList.get(0);
        double gasto = 5;
        System.out.println(trip.getBudget());
        Expenses expenses = new Expenses(trip.getId(), "a", gasto, "yen", "nada");
        addExpensive(expenses);
        ExpensesController controller = new ExpensesController(trip);

        System.out.println(controller.convertCurrency(gasto));

        System.out.println(trip.getTuristicPoint(0).getCost() + " " + trip.getTuristicPoint(0).getName());
        System.out.println(trip.getConvertedBudget());
    }

    private void addExpensive(Expenses expenses){
        Trips trips = tripsList.getFirst();
        ExpensesController controller = new ExpensesController(trips);
        controller.addExpensive(expenses);
    }

    private void menu(){
        Scanner scanner = new Scanner(System.in);
        User user = new User("Guilherme");
        System.out.println("Criando uma nova viagem");
        System.out.println("NOME:");
        String nome = scanner.nextLine();
        System.out.println("orcamento:");
        double budget = scanner.nextDouble();
        System.out.println("DESCRICAO");
        String descricao = scanner.nextLine();
        System.out.println("DESTINO");
        String destino = scanner.nextLine();
        System.out.println("MOEDA DO LUGAR");
        String moeda = scanner.nextLine();
        System.out.println("DIA DE INICIO/nex:2026-04-18");
        String datePorExtenso = scanner.nextLine();
        LocalDate dataConvertida = LocalDate.parse(datePorExtenso);
        System.out.println("DIA FINAL/nex:2026-04-18");
        String dataFinalPorExtenso = scanner.nextLine();
        LocalDate dataFinalConvertida = LocalDate.parse(dataFinalPorExtenso);

        Trips trip = new Trips(
                nome,
                budget,
                descricao,
                destino,
                moeda,
                dataConvertida,
                dataFinalConvertida,
                user
        );

        tripsList.add(trip);
    }

    private void criarDadosFalsos(){
        Trips trips = new Trips(
                "japao",
                2000,
                "nada",
                "japao",
                "yen",
                LocalDate.now(),
                LocalDate.now(),
                new User("gui")
        );

        TuristicPoint turisticPoint = new TuristicPoint(135, "tah mahal");
        trips.addTuristicPoint(turisticPoint);

        tripsList.add(trips);
    }
}
