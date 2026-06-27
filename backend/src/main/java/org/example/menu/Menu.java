package org.example.menu;

import org.example.daily.DailyBudget;
import org.example.expenses.Expenses;
import org.example.trip.Trip;
import org.example.trip.TripController;
import org.example.users.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Menu {
    TripController tripController;
    User user;

    public Menu(){
        tripController = new TripController();
        user = tripController.getUser();
    }

    public void mainScreen(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("===============================");
            System.out.println("Bem vindo " + user.getFullName());
            System.out.println("===============================");
            System.out.println(" ");
            if(tripController.hasTripOnGoing()){
                showCurrentTrip(scanner);
            } else if(tripController.getTripList().isEmpty()){
                System.out.println("Nenhuma viagem no banco");
                System.out.println("Criando viagem...");
                createTrip(scanner);
            }else {
                menu(scanner);
            }
        }
    }

    private void menu(Scanner scanner){
        int opcao=-1;
        while (true){
            System.out.println("---=====Menu=====---");
            System.out.println("Verificar lista de viagens (Pressione 1)");
            System.out.println("Criar viagem (Pressione 2)");
            System.out.println("Verificar viagem atual (Pressione 3)");
            System.out.println(" ");
            System.out.println("Sair (Pressione 0)");

            opcao = readInt(scanner, "");

            switch (opcao){
                case 1: showTrips(scanner); break;
                case 2: createTrip(scanner); break;
                case 3: showCurrentTrip(scanner); break;
                case 0: System.exit(0); break;
            }
        }
    }

    private void createTrip(Scanner scanner){
        System.out.println("----======= Criacao de viagem =======----");
        System.out.println("Titulo da viagem:");
        String name = scanner.nextLine();
        double budget = readDoubles(scanner, "Orcamento da viagem em real:");
        System.out.println("Qual o objetivo da viagem?");
        String description = scanner.nextLine();
        System.out.println("Pra onde a viagem sera?");
        String country = scanner.nextLine();
        System.out.println("Cambio da moeda (padrao global ISO 4217)");
        String currency = scanner.nextLine();
        DateTimeFormatter molde = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        System.out.println("Data de inicio (dd/mm/aaaa):");
        String startDateString = scanner.nextLine();
        LocalDate startDate = LocalDate.parse(startDateString, molde);
        System.out.println("Data final (dd/mm/aaaa):");
        String finalDateString = scanner.nextLine();
        LocalDate finalDate = LocalDate.parse(finalDateString, molde);
        tripController.createTrip(name, budget, description, country, currency, startDate, finalDate);
    }

    private void showCurrentTrip(Scanner scanner){
        //if(!tripController.hasTripOnGoing()) scanner.nextLine();
        List<Trip> tripList = tripController.getTripList();
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

        System.out.println("===============================");
        System.out.println("Viagem " + currentTrip.getName());
        System.out.println("Orcamento restante em moeda local total: " + String.format("%.2f", currentTrip.verifyRemainBudgetTrip()));
        System.out.println("Orcamento restante de hoje em moeda local: " + String.format("%.2f", dayTripToday.verifyBudgetRemaining()));
        System.out.println("==================");
        System.out.println("Adicionar gasto (Pressione 1)");
        System.out.println("Abrir lista de gastos (Pressione 2)");
        System.out.println("Abrir detalhes da viagem (Pressione 3)");
        System.out.println("Voltar ao menu (Pressione 0)");
        int opcao = readInt(scanner, "Selecione uma opcao");

        switch (opcao){
            case 1:addExpense(scanner, currentTrip); break;
            case 2:showExpensesDay(dayTripToday);break;
            case 3:showDetails(currentTrip);break;
            case 0:return;
        }
    }

    private void showDetails(Trip currentTrip){
        System.out.println("===============================");
        System.out.println("Viagem " + currentTrip.getName());
        System.out.println("Orcamento total em " + currentTrip.getCurrency() + ": " + String.format("%.2f", currentTrip.getBudget()));
        System.out.println("Valor em real: " + currentTrip.getBudgetReal());

        System.out.println("Orcamento restante em " + currentTrip.getCurrency() + ": " + String.format("%.2f", currentTrip.verifyRemainBudgetTrip()));
        System.out.println("Valor em real: " + currentTrip.verifyRemainBudgetTripReal());

        System.out.println(currentTrip.getDescription());
        System.out.println("Viagem em: " + currentTrip.getDestination());

        System.out.println("Dias da viagem: ");
        for(DailyBudget d : currentTrip.getDailyBudgetList()){
            System.out.println("Dia " + d.getDate());
            System.out.println("Orcamento diario em " + currentTrip.getCurrency() + ": " + d.getBudget());
            System.out.println("Valor em real: " + d.getBudgetReal(currentTrip.getCurrencyValue()));
        }

        System.out.println("===============================");
        System.out.println("Gostaria de visualizar os gastos de algum dia?");
        System.out.println("Digite o numero do dia");
    }

    private void addExpense(Scanner scanner, Trip currentTrip){
        System.out.println("Item");
        String description = scanner.nextLine();
        double amount = readDoubles(scanner, "Valor do gasto");
        tripController.addExpensiveToTrip(currentTrip, description, amount);
    }

    private void showExpensesDay(DailyBudget dailyBudget){
        List<Expenses> listExpenses = dailyBudget.getListExpenses();
        for(Expenses e : listExpenses){
            System.out.println(e.getDescription() + " " + e.getAmount());
            System.out.println("valor convertido pra Real: " + String.format("%.2f", e.getConvertedAmount()));
        }
    }

    private void showTrips(Scanner scanner){
        System.out.println("Lista de viagens:");
        List<Trip> tripList = tripController.getTripList();
        for(int i=0;i<tripList.size();i++){
            Trip t = tripList.get(i);
            System.out.println( (i+1) + ". Viagem " + t.getName() + " " + t.getId());
        }
        System.out.println("Gostaria de selecionar alguma viagem?");
        int opcao = readInt(scanner, "Se sim digite o numero referente a viagem, caso nao digite '0'");
        if(opcao==0){
            return;
        } else {
            detailsTrip(tripList.get(opcao-1));
        }
    }

    private void detailsTrip(Trip trip){
        trip.setStartDate(trip.getDailyBudgetList().getFirst().getDate());
        trip.setEndDate(trip.getDailyBudgetList().getLast().getDate());
        System.out.println("Viagem " + trip.getName());
        System.out.println("Orcamento em real: " + String.format("%.2f",trip.getBudgetReal()));
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

    private double readDoubles(Scanner scanner, String message){
        while(true){
            System.out.println(message);
            String answer = scanner.nextLine();
            try {
                return Double.parseDouble(answer);
            } catch (NumberFormatException e){
                System.out.println("Erro: digite um numero valido");
            }
        }
    }

    private int readInt(Scanner scanner, String message){
        while (true){
            if(!message.isBlank()) System.out.println(message);
            String answer = scanner.nextLine().trim();
            try{
                return Integer.parseInt(answer);
            } catch (NumberFormatException e){
                System.out.println("Erro: digite uma opcao valida");
            }
        }
    }
}
