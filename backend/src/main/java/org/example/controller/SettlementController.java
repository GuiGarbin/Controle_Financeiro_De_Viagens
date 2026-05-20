package org.example.controller;

import org.example.dto.request.SettleDebtRequest;
import org.example.dto.request.response.ApiResponse;
import org.example.model.Settlement;
import org.example.service.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    // GET /api/trips/{tripId}/settlements
    @GetMapping("/trips/{tripId}/settlements")
    public ResponseEntity<ApiResponse<List<Settlement>>> findByTrip(@PathVariable String tripId) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.findByTripId(tripId)));
    }

    // POST /api/trips/{tripId}/settlements
    @PostMapping("/trips/{tripId}/settlements")
    public ResponseEntity<ApiResponse<Settlement>> create(
            @PathVariable String tripId,
            @RequestBody SettleDebtRequest request) {

        Settlement settlement = settlementService.create(
            tripId,
            request.getFromUserId(),
            request.getToUserId(),
            request.getAmount(),
            request.getCurrency(),
            request.getNote()
        );
        return ResponseEntity.ok(ApiResponse.success(settlement));
    }

    // DELETE /api/settlements/{id}
    @DeleteMapping("/settlements/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        settlementService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}