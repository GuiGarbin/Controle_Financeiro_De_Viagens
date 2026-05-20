package org.example.trip;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.repository.TripRepository;
import org.example.trip.expenses.Expenses;
import org.example.trip.expenses.TuristicPoint;
import org.example.users.User;
import org.example.repository.JsonRepository;
import org.example.dto.request.response.ApiResponse;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TripController {
    public List<Trip> tripList = new ArrayList<>();
    Trip trip;
    TripRepository jsonRepository;

    public void TripController(){
        criarDadosFalsos();
        //testeDeSaida();
        configJson();
        //testeJson();
    }

    private void configJson(){
        Path path = Path.of("viagens.json");
        ObjectMapper motor = new ObjectMapper();
        motor.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        motor.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        jsonRepository = new TripRepository(path, motor);
    }

    private void testeJson(){
        jsonRepository.save(trip);

        List<Trip> tripsjson = jsonRepository.findAll();
        System.out.println(tripsjson.getFirst().getBudgetReal());
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
        addExpensive(expenses);
        System.out.println("Saldo restante total em moeda estrangeira apos gasto: " + trip.verifyRemainBudgetTrip());
        System.out.println("Saldo restante total em moeda local apos gasto: " + trip.verifyRemainBudgetTripReal());
        System.out.println("Saldo restante do dia: " + trip.verifyDay(0).verifyBudget());

        //Teste de conversao cambial
        System.out.println("Converter gasto: " + trip.verifyDay(0).getExpense(0).getConvertedAmount());

        //Teste de dados diarios
        for(int i=0;i<trip.getDailyBudgetList().size();i++){
            System.out.println("Dia :" + trip.getDailyBudgetList().get(i).getDate());
            System.out.println("Saldo restante em moeda estrangeira: " + trip.getDailyBudgetList().get(i).verifyBudget());
            System.out.println("Saldo restante em moeda local: " + trip.getDailyBudgetList().get(i).verifyBudgetReal(trip.getCurrencyValue()));
        }

        //Teste de segundo gasto
        Expenses expenses2 = new Expenses(trip.getId(), "gasto teste", 600, trip.getCurrencyValue(), "yen", "nada");
        addExpensive(expenses2);
        System.out.println("Saldo restante total em moeda estrangeira apos gasto 2: " + trip.verifyRemainBudgetTrip());
        System.out.println("Saldo restante total em moeda local apos gasto 2: " + trip.verifyRemainBudgetTripReal());
        System.out.println("Saldo restante do dia: " + trip.verifyDay(0).verifyBudget());
    }

    private void addExpensive(Expenses expenses){
        Trip trip = tripList.getFirst();
        trip.verifyDay(0).addExpense(expenses, trip);
    }

    //classe para criar dados falsos para testes
    private void criarDadosFalsos(){
        Trip trip = new Trip(
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
                    addExpensive(new Expenses(trip.getId(), null, value, trip.getCurrencyValue(), "yen", null));
                    break;
                case 0:
                    on=false;
            }
        }
    }
}
