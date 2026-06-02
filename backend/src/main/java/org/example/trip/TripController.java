package org.example.trip;

import org.example.config.JsonStorageConfig;
import org.example.repository.TripRepository;
import org.example.daily.DailyBudget;
import org.example.expenses.Expenses;
import org.example.expenses.TuristicPoint;
import org.example.service.CurrencyService;
import org.example.users.User;
import org.springframework.core.io.AbstractResource;

import java.net.http.HttpClient;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TripController {
    public List<Trip> tripList = new ArrayList<>();
    Trip tripTest;
    TripRepository tripRepository;
    User user;
    HttpClient clienteWeb = HttpClient.newHttpClient();
    CurrencyService cambioApi = new CurrencyService();

    public void TripController(){
        //criarDadosFalsos();
        //testeDeSaida();
        //testeJson();
        configJson();
        falseUser();
        menu();
    }

    private void configJson(){
        JsonStorageConfig storageConfig = new JsonStorageConfig();
        tripRepository = storageConfig.tripRepository(storageConfig.objectMapper());
    }

    private void falseUser(){
        user = new User("Guilherme", LocalDate.now());
    }

    private void menu(){
        boolean on = true;
        int opcao=-1;
        while (on){
            Scanner scanner = new Scanner(System.in);

            System.out.println("===============================");
            System.out.println("Bem vindo " + user.getFullName());
            System.out.println("O que gostaria de fazer?");
            System.out.println("Verificar lista de viagens (Pressione 1)");
            System.out.println("Criar viagem (Pressione 2)");
            System.out.println("Verificar viagem atual (Pressione 3)");
            System.out.println("Sair (Pressione 0)");

            opcao = scanner.nextInt();

            switch (opcao){
                case 1: showTrips(scanner); break;
                case 2: createTrip(scanner); break;
                case 3: showCurrentTrip(scanner); break;
                case 0: on = false; break;
            }
        }
    }

    private void showCurrentTrip(Scanner scanner){
        scanner.nextLine();
        tripList = tripRepository.findAll();
        Trip currentTrip = null;
        for(Trip trip : tripList){
            if(trip.isStatus()){
                currentTrip = trip;
                break;
            }
        }
        if(currentTrip==null){
            System.out.println("Nenhuma viagem em andamento");
            return;
        }
        List<DailyBudget> listDays = currentTrip.getDailyBudgetList();
        DailyBudget dayTripToday = null;
        for(DailyBudget d : listDays){
            if(d.getDate().equals(LocalDate.now())){
                dayTripToday = d;
            }
        }
        if (dayTripToday==null){
            System.out.println("Erro ao abrir dias da viagem");
            return;
        }

        System.out.println("Viagem " + currentTrip.getName());
        System.out.println("Orcamento restante total: " + String.format("%.2f", currentTrip.verifyRemainBudgetTrip()));
        System.out.println("Orcamento restante de hoje: " + String.format("%.2f", dayTripToday.verifyBudgetRemaining()));
        System.out.println("==================");
        System.out.println("Adicionar gasto (Pressione 1)");
        System.out.println("Abrir lista de gastos (Pressione 2)");
        int opcao = scanner.nextInt();

        switch (opcao){
            case 1:addExpensiveToTrip(scanner, currentTrip); break;
            case 2:showExpensesDay(dayTripToday);break;
        }
    }

    private void showExpensesDay(DailyBudget dailyBudget){
        List<Expenses> listExpenses = dailyBudget.getListExpenses();
        for(Expenses e : listExpenses){
            System.out.println(e.getDescription() + " " + e.getAmount());
            System.out.println("valor convertido pra Real: " + String.format("%.2f", e.getConvertedAmount()));
        }
    }

    private void addExpensiveToTrip(Scanner scanner, Trip trip){
        scanner.nextLine();
        System.out.println("Item");
        String description = scanner.nextLine();
        System.out.println("Valor do gasto");
        double amount = scanner.nextDouble();
        double currentCurrency = cambioApi.getRate(trip.getCurrency(), "BRL");
        Expenses expenses = new Expenses(trip.getId(), description, amount, currentCurrency, trip.getCurrency(), "");
        LocalDate today = LocalDate.now();
        for(DailyBudget d : trip.getDailyBudgetList()){
            if(d.getDate().equals(today)){
                System.out.println("achou");
                d.addExpense(expenses);
                if(d.verifyBudgetRemaining() < 0){
                    System.out.println("Voce passou do orcamento diario");
                } else if(d.verifyBudgetRemaining()>=(d.getBudget()*0.9) && d.verifyBudgetRemaining()<d.getBudget()){
                    System.out.println("Voce esta chegando perto do limite diario");
                }
            }
        }
        tripRepository.save(trip);
    }

    private void showTrips(Scanner scanner){
        scanner.nextLine();
        System.out.println("Lista de viagens:");
        tripList = tripRepository.findAll();
        for(Trip t : tripList){
            System.out.println("Viagem " + t.getName() + " " + t.getId());
        }
        System.out.println("Gostaria de selecionar alguma viagem?");
        System.out.println("Se sim digite o id, caso nao digite '0'");
        String opcao = scanner.nextLine();
        if(opcao.equals("0")){
            return;
        } else {
            for (Trip t : tripList){
                if(t.getId().equals(opcao)){
                    detailsTrip(t);
                    break;
                }
            }
        }
    }

    private void detailsTrip(Trip trip){
        trip.setStartDate(trip.getDailyBudgetList().getFirst().getDate());
        trip.setEndDate(trip.getDailyBudgetList().getLast().getDate());
        System.out.println("Viagem " + trip.getName());
        System.out.println("Orcamento: " + trip.getBudgetReal());
        if(trip.getStartDate().isBefore(LocalDate.now()) && trip.getEndDate().isAfter(LocalDate.now())) {
            System.out.println("Viagem em andamento");
        } else if(trip.getStartDate().isAfter(LocalDate.now()) && trip.isStatus()){
            System.out.println("Proxima viagem planejada");
        } else if(trip.getEndDate().isBefore(LocalDate.now())){
            System.out.println("Viagem encerrada");
        } else if(trip.getStartDate().isAfter(LocalDate.now())&&!trip.isStatus()){
            System.out.println("Viagem salva");
        }
    }

    private void createTrip(Scanner scanner){
        scanner.nextLine();
        System.out.println("Titulo da viagem:");
        String name = scanner.nextLine();
        System.out.println("Orcamento da viagem:");
        double budget = scanner.nextDouble();
        scanner.nextLine();
        System.out.println("Qual o objetivo da viagem?");
        String description = scanner.nextLine();
        System.out.println("Pra onde a viagem sera?");
        String country = scanner.nextLine();
        System.out.println("Cambio da moeda");
        String currency = scanner.nextLine();
        System.out.println("Data de inicio");
        String startDateString = scanner.nextLine();
        LocalDate startDate = LocalDate.parse(startDateString);
        System.out.println("Data final");
        String finalDateString = scanner.nextLine();
        LocalDate finalDate = LocalDate.parse(finalDateString);

        Trip trip1 = new Trip(
                name,
                budget,
                description,
                country,
                currency,
                startDate,
                finalDate,
                user
        );

        for(Trip trip : tripList){
            if(trip.isStatus()){
                trip.setStatus(false);
                tripRepository.save(trip);
            }
        }
        tripRepository.save(trip1);
        System.out.println("Viagem criada com sucesso!");
    }

    //classe teste para se a saida esta como deveria
    private void testeDeSaida(){
        //Teste de saldo inicial
        System.out.println("Saldo inicial em moeda estrangeira: " + tripTest.getInitialBudget());
        System.out.println("Saldo inicial em moeda local: " + tripTest.getBudgetReal());
        System.out.println("Saldo total em moeda estrangeira: " + tripTest.verifyRemainBudgetTrip());
        System.out.println("Saldo total em moeda local: " + tripTest.verifyRemainBudgetTripReal());

        //Teste de divisao de orcamento por dia
        System.out.println("Saldo diario em moeda estrangeira: " + tripTest.verifyDay(0).getBudget());
        System.out.println("Saldo diario em moeda local: " + tripTest.verifyDay(0).getBudgetReal(tripTest.getCurrencyValue()));

        //Teste de adicao de gasto (somente diario)
        Expenses expenses = new Expenses(tripTest.getId(), "gasto teste", 300, tripTest.getCurrencyValue(), "yen", "nada");
        //addExpensive(expenses);
        System.out.println("Saldo restante total em moeda estrangeira apos gasto: " + tripTest.verifyRemainBudgetTrip());
        System.out.println("Saldo restante total em moeda local apos gasto: " + tripTest.verifyRemainBudgetTripReal());
        System.out.println("Saldo restante do dia: " + tripTest.verifyDay(0).verifyBudgetRemaining());

        //Teste de conversao cambial
        System.out.println("Converter gasto: " + tripTest.verifyDay(0).getExpense(0).getConvertedAmount());

        //Teste de dados diarios
        for(int i = 0; i< tripTest.getDailyBudgetList().size(); i++){
            System.out.println("Dia :" + tripTest.getDailyBudgetList().get(i).getDate());
            System.out.println("Saldo restante em moeda estrangeira: " + tripTest.getDailyBudgetList().get(i).verifyBudgetRemaining());
            System.out.println("Saldo restante em moeda local: " + tripTest.getDailyBudgetList().get(i).verifyBudgetReal(tripTest.getCurrencyValue()));
        }

        //Teste de segundo gasto
        Expenses expenses2 = new Expenses(tripTest.getId(), "gasto teste", 600, tripTest.getCurrencyValue(), "yen", "nada");
        //addExpensive(expenses2);
        System.out.println("Saldo restante total em moeda estrangeira apos gasto 2: " + tripTest.verifyRemainBudgetTrip());
        System.out.println("Saldo restante total em moeda local apos gasto 2: " + tripTest.verifyRemainBudgetTripReal());
        System.out.println("Saldo restante do dia: " + tripTest.verifyDay(0).verifyBudgetRemaining());
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
        this.tripTest = tripList.get(0);
    }

    private void testeJson(){
        //jsonRepository.save(trip);

        List<Trip> tripsjson = tripRepository.findAll();
        tripTest = tripsjson.getFirst();
        System.out.println(tripsjson.getFirst().getBudgetReal());
        System.out.println(tripsjson.getFirst().verifyDay(1).verifyBudgetRemaining());
        addExpensive(new Expenses(tripTest.getId(), "gasto teste", 300, tripTest.getCurrencyValue(), "yen", "nada"), tripsjson.getFirst());
        tripRepository.save(tripTest);

        tripsjson = tripRepository.findAll();
        Trip tripJSon = tripsjson.getFirst();
        System.out.println(tripJSon.verifyDay(1).verifyBudgetRemaining());
    }

    private void addExpensive(Expenses expenses, Trip trip){

    }
}
