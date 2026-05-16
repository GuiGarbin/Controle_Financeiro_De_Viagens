package org.example.service;

import org.example.exception.ResourceNotFoundException;
import org.example.exception.ValidationException;
import org.example.model.Settlement;
import org.example.repository.SettlementRepository;
import org.example.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SettlementService { // Serviço para gerenciar acertos entre usuários, permitindo criar, listar e remover acertos de uma viagem

    private final SettlementRepository settlementRepository;

    public SettlementService(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    // Retorna todos os acertos de uma viagem
    public List<Settlement> findByTripId(String tripId) {
        return settlementRepository.findByTripId(tripId);
    }

    // Registra um novo acerto
    public Settlement create(String tripId, String fromUserId,
                              String toUserId, double amount,
                              String currency, String note) {

        if (amount <= 0) {
            throw new ValidationException("O valor do acerto deve ser maior que zero");
        }
        if (fromUserId.equals(toUserId)) {
            throw new ValidationException("Os usuários devem ser diferentes");
        }

        Settlement settlement = new Settlement();
        settlement.setId(IdGenerator.forSettlement());
        settlement.setTripId(tripId);
        settlement.setFromUserId(fromUserId);
        settlement.setToUserId(toUserId);
        settlement.setAmount(amount);
        settlement.setCurrency(currency);
        settlement.setNote(note);
        settlement.setSettledAt(Instant.now().toString());
        settlement.setCreatedAt(Instant.now().toString());
        settlement.setUpdatedAt(Instant.now().toString());

        return settlementRepository.save(settlement);
    }

    // Remove um acerto
    public void delete(String id) {
        settlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Acerto não encontrado: " + id));
        settlementRepository.deleteById(id);
    }
}