package org.example.controller;

import org.example.dto.request.AddExpenseRequest;
import org.example.dto.request.AddTuristicPointRequest;
import org.example.dto.request.TripCreateRequest;
import org.example.dto.request.response.AddExpenseResponse;
import org.example.dto.request.response.ApiResponse;
import org.example.expenses.Expenses;
import org.example.expenses.TuristicPoint;
import org.example.service.TripService;
import org.example.trip.Trip;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controller REST de viagens
// Nome "TripRestController" para nao colidir com a classe de negocio do
// terminal org.example.trip.TripController (que NAO e um controller HTTP).
//
// O usuario logado e identificado pelo header opcional X-User-Id (curto prazo,
// app desktop mono-usuario). Quando presente, as viagens sao filtradas/atribuidas
// ao dono; quando ausente (uso em dev/curl), o servico opera sobre todas.
@RestController
@RequestMapping("/api/trips")
public class TripRestController {

    private final TripService tripService;

    public TripRestController(TripService tripService) {
        this.tripService = tripService;
    }

    // POST /api/trips — cria viagem (gera os DailyBudget e resolve o cambio).
    @PostMapping
    public ResponseEntity<ApiResponse<Trip>> create(
            @RequestBody TripCreateRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Trip trip = tripService.createTrip(request, userId);
        return ResponseEntity.ok(ApiResponse.success(trip));
    }

    // GET /api/trips — lista as viagens do usuario (ou todas, sem header).
    @GetMapping
    public ResponseEntity<ApiResponse<List<Trip>>> list(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(ApiResponse.success(tripService.getTrips(userId)));
    }

    // GET /api/trips/current — viagem em andamento (status=true).
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Trip>> current(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(ApiResponse.success(tripService.getCurrentTrip(userId)));
    }

    // GET /api/trips/{id} — detalhe de uma viagem (alimenta o Dashboard).
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Trip>> getById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(tripService.getTripById(id)));
    }

    // PUT /api/trips/{id}/activate — define qual viagem e a atual.
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Trip>> activate(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(tripService.activateTrip(id)));
    }

    // DELETE /api/trips/{id} — remove a viagem.
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        tripService.deleteTrip(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // POST /api/trips/{id}/expenses — adiciona gasto na data informada.
    @PostMapping("/{id}/expenses")
    public ResponseEntity<ApiResponse<AddExpenseResponse>> addExpense(
            @PathVariable String id,
            @RequestBody AddExpenseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(tripService.addExpense(id, request)));
    }

    // GET /api/trips/{id}/expenses — lista gastos (opcionalmente filtrando por ?date=dd/MM/yyyy).
    @GetMapping("/{id}/expenses")
    public ResponseEntity<ApiResponse<List<Expenses>>> listExpenses(
            @PathVariable String id,
            @RequestParam(value = "date", required = false) String date) {
        return ResponseEntity.ok(ApiResponse.success(tripService.getExpenses(id, date)));
    }

    // POST /api/trips/{id}/turistic-points — adiciona ponto turístico; devolve a lista atualizada.
    @PostMapping("/{id}/turistic-points")
    public ResponseEntity<ApiResponse<List<TuristicPoint>>> addTuristicPoint(
            @PathVariable String id,
            @RequestBody AddTuristicPointRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                tripService.addTuristicPoint(id, request.getName(), request.getCost())));
    }

    // GET /api/trips/{id}/turistic-points — lista os pontos turísticos da viagem.
    @GetMapping("/{id}/turistic-points")
    public ResponseEntity<ApiResponse<List<TuristicPoint>>> listTuristicPoints(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(tripService.getTuristicPoints(id)));
    }
}
