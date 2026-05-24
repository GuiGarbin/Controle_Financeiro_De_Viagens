package org.example.trip;

import org.example.config.JsonStorageConfig;
import org.example.repository.TripRepository;
import org.example.trip.daily.DailyBudget;
import org.example.trip.expenses.Expenses;
import org.example.trip.expenses.TuristicPoint;
import org.example.users.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TripController {
    public List<Trip> tripList = new ArrayList<>();
    Trip trip;
    TripRepository tripRepository;

    public void TripController(){
        //criarDadosFalsos();
        //testeDeSaida();
        configJson();
        testeJson();
    }

    private void configJson(){
        JsonStorageConfig storageConfig = new JsonStorageConfig();
        tripRepository = storageConfig.tripRepository(storageConfig.objectMapper());
    }

    private void testeJson(){
        //jsonRepository.save(trip);

        List<Trip> tripsjson = tripRepository.findAll();
        trip = tripsjson.getFirst();
        System.out.println(tripsjson.getFirst().getBudgetReal());
        System.out.println(tripsjson.getFirst().verifyDay(1).verifyBudgetRemaining());
        addExpensive(new Expenses(trip.getId(), "gasto teste", 300, trip.getCurrencyValue(), "yen", "nada"), tripsjson.getFirst());
        tripRepository.save(trip);

        tripsjson = tripRepository.findAll();
        Trip tripJSon = tripsjson.getFirst();
        System.out.println(tripJSon.verifyDay(1).verifyBudgetRemaining());
    }

    private void addExpensive(Expenses expenses, Trip trip){
        LocalDate today = LocalDate.now();
        for(DailyBudget d : trip.getDailyBudgetList()){
            if(d.getDate().equals(today)){
                System.out.println("achou");
                d.addExpense(expenses);
                if(d.getBudget()>=d.verifyBudgetRemaining()){
                    System.out.println("Voce passou do orcamento diario");
                } else if(d.verifyBudgetRemaining()>=(d.getBudget()*0.9) && d.verifyBudgetRemaining()<d.getBudget()){
                    System.out.println("Voce esta chegando perto do limite diario");
                }
            }
        }
    }

    private void addTrip(User user, LocalDate startDate, LocalDate endDate){
        Scanner scanner = new Scanner(System.in);
        String name = scanner.nextLine();
        int budgetReal = scanner.nextInt();
        String description = scanner.nextLine();
        String destination = scanner.nextLine();
        String currency = scanner.nextLine();

        Trip newTrip = new Trip(name,
                                budgetReal,
                                description,
                                destination,
                                currency,
                                startDate,
                                endDate,
                                user
        );

        tripRepository.save(newTrip);
    }

    //classe teste para se a saida esta como deveria
    private void testeDeSaida(){
        //Teste de saldo inicial
        System.out.println("Saldo inicial em moeda estrangeira: " + trip.getInitialBudget());
        System.out.println("Saldo inicial em moeda local: " + trip.getBudgetReal());
        System.out.println("Saldo total em moeda estrangeira: " + trip.verifyRemainBudgetTrip());
        System.out.println("Saldo total em moeda local: " + trip.verifyRemainBudgetTripReal());

        //Teste de divisao de orcamento por dia
        System.out.println("Saldo diario em moeda estrangeira: " + trip.verifyDay(0).getBudget());
        System.out.println("Saldo diario em moeda local: " + trip.verifyDay(0).getBudgetReal(trip.getCurrencyValue()));

        //Teste de adicao de gasto (somente diario)
        Expenses expenses = new Expenses(trip.getId(), "gasto teste", 300, trip.getCurrencyValue(), "yen", "nada");
        //addExpensive(expenses);
        System.out.println("Saldo restante total em moeda estrangeira apos gasto: " + trip.verifyRemainBudgetTrip());
        System.out.println("Saldo restante total em moeda local apos gasto: " + trip.verifyRemainBudgetTripReal());
        System.out.println("Saldo restante do dia: " + trip.verifyDay(0).verifyBudgetRemaining());

        //Teste de conversao cambial
        System.out.println("Converter gasto: " + trip.verifyDay(0).getExpense(0).getConvertedAmount());

        //Teste de dados diarios
        for(int i=0;i<trip.getDailyBudgetList().size();i++){
            System.out.println("Dia :" + trip.getDailyBudgetList().get(i).getDate());
            System.out.println("Saldo restante em moeda estrangeira: " + trip.getDailyBudgetList().get(i).verifyBudgetRemaining());
            System.out.println("Saldo restante em moeda local: " + trip.getDailyBudgetList().get(i).verifyBudgetReal(trip.getCurrencyValue()));
        }

        //Teste de segundo gasto
        Expenses expenses2 = new Expenses(trip.getId(), "gasto teste", 600, trip.getCurrencyValue(), "yen", "nada");
        //addExpensive(expenses2);
        System.out.println("Saldo restante total em moeda estrangeira apos gasto 2: " + trip.verifyRemainBudgetTrip());
        System.out.println("Saldo restante total em moeda local apos gasto 2: " + trip.verifyRemainBudgetTripReal());
        System.out.println("Saldo restante do dia: " + trip.verifyDay(0).verifyBudgetRemaining());
    }



    //classe para criar dados falsos para testes
    private void criarDadosFalsos(){
        Trip trip = new Trip(
                "japao",
                2000,
                "nada",
                "japao",
                "yen",
                LocalDate.parse("2026-05-18"),
                LocalDate.parse("2026-05-20"),
                new User("gui")
        );

        TuristicPoint turisticPoint = new TuristicPoint(135, "tah mahal");
        trip.addTuristicPoint(turisticPoint);

        tripList.add(trip);
        this.trip = tripList.get(0);
    }

    private void menu(){
        boolean on = true;
        while (on){
            Scanner scanner = new Scanner(System.in);
            System.out.println("=====================");
            System.out.println("Orcamento total: " + trip.getInitialBudget());
            System.out.println("Conferir dia: ");
            for (int i=0;i<trip.getDailyBudgetList().size();i++){
                System.out.println("Dia " + i + ": " + trip.getDailyBudgetList().get(i).getDate());
                System.out.println("Orcamento: " + trip.getDailyBudgetList().get(i).getBudget());
                //System.out.println("Orcamento convertido: " + trip.getDailyBudgetList().get(i).getConvertedBudget());
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
                    //addExpensive(new Expenses(trip.getId(), null, value, trip.getCurrencyValue(), "yen", null));
                    break;
                case 0:
                    on=false;
            }
        }
    }
}
