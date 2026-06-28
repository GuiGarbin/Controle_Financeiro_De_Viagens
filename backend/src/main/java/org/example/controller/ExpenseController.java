package org.example.controller;

import org.example.dto.request.CreateExpenseRequest;
import org.example.dto.request.response.ApiResponse;
import org.example.model.Expense;
import org.example.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ARQUIVADO: esta camada REST "plana"
// nao e mais consumida pelo frontend e mapeava /api/trips/{tripId}/expenses, o
// que colidiria com o novo TripRestController (modelo do terminal e a fonte da
// verdade). As anotacoes @RestController/@RequestMapping foram removidas para
// desregistrar os endpoints e evitar "ambiguous mapping" na subida do Spring.
// O codigo permanece aqui apenas como referencia, sem ser um bean.
//@RestController
//@RequestMapping("/api")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // GET /api/trips/{tripId}/expenses
    @GetMapping("/trips/{tripId}/expenses")
    public ResponseEntity<ApiResponse<List<Expense>>> findByTrip(@PathVariable String tripId) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.findByTripId(tripId)));
    }

    // GET /api/expenses/{id}
    @GetMapping("/expenses/{id}")
    public ResponseEntity<ApiResponse<Expense>> findById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(expenseService.findById(id)));
    }

    // POST /api/trips/{tripId}/expenses
    @PostMapping("/trips/{tripId}/expenses")
    public ResponseEntity<ApiResponse<Expense>> create(
            @PathVariable String tripId,
            @RequestBody CreateExpenseRequest request) {

        Expense expense = expenseService.create(
            tripId,
            request.getDescription(),
            request.getAmount(),
            request.getCurrency(),
            request.getCategoryId(),
            request.getPaidByUserId(),
            request.getSplitMethod(),
            request.getSplits() != null
                ? request.getSplits().stream()
                    .map(s -> { var m = s.getUserId(); return m; })
                    .toList()
                : List.of(),
            request.getSplits(),
            request.getDate(),
            request.getNotes()
        );
        return ResponseEntity.ok(ApiResponse.success(expense));
    }

    // DELETE /api/expenses/{id}
    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        expenseService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}