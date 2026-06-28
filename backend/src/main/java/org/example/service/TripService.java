package org.example.service;

import org.example.daily.DailyBudget;
import org.example.dto.request.AddExpenseRequest;
import org.example.dto.request.TripCreateRequest;
import org.example.dto.request.response.AddExpenseResponse;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.ValidationException;
import org.example.expenses.Expenses;
import org.example.repository.TripRepository;
import org.example.trip.Trip;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Servico gerenciado pelo Spring que concentra a logica de viagens (antes presa
// em trip.TripController, que nao era um bean e usava um usuario falso).
// Recebe TripRepository e CurrencyService por injecao de dependencia.
@Service
public class TripService {

    // Formato usado em todo o app (dd/MM/yyyy), igual ao configurado no ObjectMapper.
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String LOCAL_CURRENCY = "BRL";

    private final TripRepository tripRepository;
    private final CurrencyService currencyService;

    public TripService(TripRepository tripRepository, CurrencyService currencyService) {
        this.tripRepository = tripRepository;
        this.currencyService = currencyService;
    }

    // --- Viagens (Fase 1) ---

    // Cria uma viagem, gera os DailyBudget automaticamente (no construtor de Trip),
    // resolve o cambio e define o dono (createdById). A nova viagem entra como
    // "atual" (status=true) e as demais do mesmo dono sao desativadas.
    public Trip createTrip(TripCreateRequest request, String userId) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("O nome da viagem e obrigatorio.");
        }
        if (request.getBudget() <= 0) {
            throw new ValidationException("O orcamento deve ser maior que zero.");
        }

        LocalDate startDate = parseDate(request.getStartDate(), "data de inicio");
        LocalDate endDate = parseDate(request.getEndDate(), "data de fim");
        if (endDate.isBefore(startDate)) {
            throw new ValidationException("A data de fim nao pode ser anterior a data de inicio.");
        }

        double currencyValue = currencyService.getRate(request.getCurrency(), LOCAL_CURRENCY);

        Trip trip = new Trip(
                request.getName(),
                request.getBudget(),
                request.getDescription(),
                request.getDestination(),
                request.getCurrency(),
                currencyValue,
                startDate,
                endDate,
                userId
        );

        deactivateOtherTrips(null, userId);
        tripRepository.save(trip);
        return trip;
    }

    // Lista as viagens. Quando o userId e informado, retorna apenas as do dono;
    // sem userId (uso em dev), retorna todas.
    public List<Trip> getTrips(String userId) {
        List<Trip> all = tripRepository.findAll();
        if (userId == null || userId.isBlank()) {
            return all;
        }
        List<Trip> mine = new ArrayList<>();
        for (Trip t : all) {
            if (userId.equals(t.getCreatedById())) {
                mine.add(t);
            }
        }
        return mine;
    }

    // Retorna a viagem em andamento (status=true) do usuario, ou 404 se nao houver.
    public Trip getCurrentTrip(String userId) {
        for (Trip t : getTrips(userId)) {
            if (t.isStatus()) {
                return t;
            }
        }
        throw new ResourceNotFoundException("Nenhuma viagem em andamento.");
    }

    public Trip getTripById(String id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Viagem nao encontrada: " + id));
    }

    // Define qual viagem e a atual: ativa a escolhida e desativa as outras do mesmo dono.
    public Trip activateTrip(String id) {
        Trip trip = getTripById(id);
        deactivateOtherTrips(trip.getId(), trip.getCreatedById());
        trip.setStatus(true);
        tripRepository.save(trip);
        return trip;
    }

    public void deleteTrip(String id) {
        // Garante que existe antes de remover (senao retorna 404).
        getTripById(id);
        tripRepository.deleteById(id);
    }

    // --- Gastos (Fase 2) ---

    // Adiciona um gasto ao dia informado (nao apenas "hoje"). Valida que a data
    // cai dentro do intervalo da viagem e devolve a verificacao de orcamento.
    public AddExpenseResponse addExpense(String tripId, AddExpenseRequest request) {
        Trip trip = getTripById(tripId);

        if (request.getAmount() <= 0) {
            throw new ValidationException("O valor do gasto deve ser maior que zero.");
        }
        LocalDate date = parseDate(request.getDate(), "data do gasto");

        DailyBudget day = findDayByDate(trip, date);
        if (day == null) {
            throw new ValidationException(
                    "A data " + request.getDate() + " esta fora do intervalo da viagem.");
        }

        double rate = currencyService.getRate(trip.getCurrency(), LOCAL_CURRENCY);
        Expenses expense = new Expenses(
                trip.getId(),
                request.getDescription(),
                request.getAmount(),
                rate,
                trip.getCurrency(),
                request.getNotes() == null ? "" : request.getNotes());
        expense.setDate(date);
        day.addExpense(expense);

        tripRepository.save(trip);

        return buildExpenseResponse(trip, day, expense);
    }

    // Lista os gastos da viagem. Com a data informada (dd/MM/yyyy), filtra pelo dia.
    public List<Expenses> getExpenses(String tripId, String dateFilter) {
        Trip trip = getTripById(tripId);
        LocalDate filter = (dateFilter == null || dateFilter.isBlank())
                ? null
                : parseDate(dateFilter, "data do filtro");

        List<Expenses> result = new ArrayList<>();
        for (DailyBudget day : trip.getDailyBudgetList()) {
            if (filter != null && !filter.equals(day.getDate())) {
                continue;
            }
            result.addAll(day.getListExpenses());
        }
        return result;
    }

    // --- Auxiliares ---

    // Desativa as viagens ativas do mesmo dono, exceto a viagem indicada (se houver).
    private void deactivateOtherTrips(String keepTripId, String userId) {
        for (Trip t : tripRepository.findAll()) {
            boolean sameOwner = userId == null || userId.equals(t.getCreatedById());
            if (t.isStatus() && sameOwner && !t.getId().equals(keepTripId)) {
                t.setStatus(false);
                tripRepository.save(t);
            }
        }
    }

    private DailyBudget findDayByDate(Trip trip, LocalDate date) {
        for (DailyBudget day : trip.getDailyBudgetList()) {
            if (date.equals(day.getDate())) {
                return day;
            }
        }
        return null;
    }

    private AddExpenseResponse buildExpenseResponse(Trip trip, DailyBudget day, Expenses expense) {
        AddExpenseResponse response = new AddExpenseResponse();
        response.setExpense(expense);

        double dailyRemaining = day.verifyBudgetRemaining();
        response.setDailyBudgetRemaining(dailyRemaining);
        response.setTripBudgetRemaining(trip.verifyRemainBudgetTrip());

        if (dailyRemaining < 0) {
            response.setOverBudget(true);
            response.setWarning("Voce passou do orcamento diario.");
        } else if (dailyRemaining <= (day.getBudget() * 0.1)) {
            response.setNearLimit(true);
            response.setWarning("Voce esta chegando perto do limite diario.");
        }
        return response;
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("A " + fieldName + " e obrigatoria (formato dd/MM/yyyy).");
        }
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (Exception e) {
            throw new ValidationException("A " + fieldName + " e invalida (use o formato dd/MM/yyyy).");
        }
    }
}
