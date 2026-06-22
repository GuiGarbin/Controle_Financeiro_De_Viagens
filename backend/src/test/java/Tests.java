import org.example.expenses.Expenses;
import org.example.model.Expense;
import org.example.service.CurrencyService;
import org.example.trip.Trip;
import org.example.trip.TripController;
import org.example.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

public class Tests {

    private Trip trip;
    private User user;
    private CurrencyService cambioApi = new CurrencyService();
    private TripController tripController = new TripController();

    @BeforeEach
    void setUp() {
        user = new User("Guilherme", LocalDate.now());

        trip = new Trip(
                "Projeto Férias",
                5000.00,
                "Descanso",
                "Japão",
                "JPY",
                cambioApi.getRate("JPY", "BRL"),
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(15),
                user
        );
    }

    @Test
    void deveRetornarCambio(){
        double resultado = cambioApi.getRate("JPY", "BRL");

        assertEquals(0.032, resultado, 0.003);
    }

    @Test
    void deveGarantirOrcamentoInicialIntacto() {
        double resultado = trip.getBudgetReal();

        assertEquals(5000.00, resultado, 0.01);
    }

    @Test
    void deveGarantirOrcamentoInicialConvertido(){
        double resultado = trip.getBudget();

        assertEquals(156546.61, resultado, 400);
    }


    @Test
    void deveMostrarOrcamentoDepoisDeGastoReal(){
        Expenses expense = new Expenses(
                                trip.getId(),
                                "comida",
                                300,
                                trip.getCurrencyValue(),
                                trip.getCurrency(),
                                "");
        trip.getDailyBudgetList().getFirst().addExpense(expense);

        assertEquals(4990.42, trip.verifyRemainBudgetTripReal(), 0.2);
    }

    @Test
    void deveMostrarOrcamentoDepoisDeGastoConvertido(){
        Expenses expense = new Expenses(
                trip.getId(),
                "comida",
                300,
                trip.getCurrencyValue(),
                trip.getCurrency(),
                "");
        trip.getDailyBudgetList().getFirst().addExpense(expense);

        assertEquals(156246.75, trip.verifyRemainBudgetTrip(), 400);
    }

    @Test
    void deveRetornarDiasQueAViagemPossui(){
        double resultado = 11;

        assertEquals(trip.getDailyBudgetList().size(), resultado, 0.1);
    }

    @Test
    void deveAlterarStatusDaViagemParaFalso() {
        trip.setStatus(false);

        assertFalse(trip.isStatus());
    }

    @Test
    void deveValidarSeDataInicialEAnteriorADataFinal() {
        LocalDate inicio = trip.getStartDate();
        LocalDate fim = trip.getEndDate();

        assertTrue(inicio.isBefore(fim));
    }

}