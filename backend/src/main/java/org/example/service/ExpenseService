package org.example.service;

import org.example.exception.ResourceNotFoundException;
import org.example.exception.ValidationException; 
import org.example.model.Expense; 
import org.example.model.enums.SplitMethod; 
import org.example.repository.ExpenseRepository; 
import org.example.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseService { // Serviço para gerenciar despesas, permitindo criar, listar e remover despesas de uma viagem

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    // Retorna todas as despesas de uma viagem
    public List<Expense> findByTripId(String tripId) {
        return expenseRepository.findByTripId(tripId);
    }

    // Busca uma despesa pelo id
    public Expense findById(String id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada: " + id));
    }

    // Cria uma nova despesa
    public Expense create(String tripId, String description, double amount,
                          String currency, String categoryId, String paidByUserId,
                          SplitMethod splitMethod, List<String> memberIds,
                          List<Expense.Split> splits, String date, String notes) {

        // Validações básicas
        if (amount <= 0) {
            throw new ValidationException("O valor da despesa deve ser maior que zero");
        }
        if (description == null || description.isBlank()) {
            throw new ValidationException("A descrição é obrigatória");
        }

        // Calcula splits automaticamente se o método for EQUAL
        if (splitMethod == SplitMethod.EQUAL) {
            splits = calculateEqualSplits(amount, memberIds);
        } else {
            // Valida se os splits informados somam o total
            validateSplits(amount, splits);
        }

        Expense expense = new Expense();
        expense.setId(IdGenerator.forExpense());
        expense.setTripId(tripId);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCurrency(currency);
        expense.setCategoryId(categoryId);
        expense.setPaidByUserId(paidByUserId);
        expense.setSplitMethod(splitMethod);
        expense.setSplits(splits);
        expense.setDate(date);
        expense.setNotes(notes);
        expense.setCreatedAt(Instant.now().toString());
        expense.setUpdatedAt(Instant.now().toString());

        return expenseRepository.save(expense);
    }

    // Remove uma despesa
    public void delete(String id) {
        findById(id);
        expenseRepository.deleteById(id);
    }

    // Divide o valor igualmente entre os membros
    private List<Expense.Split> calculateEqualSplits(double amount, List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            throw new ValidationException("É necessário pelo menos um membro para dividir");
        }

        int count = memberIds.size();
        // Arredonda para 2 casas decimais
        double share = Math.floor((amount / count) * 100) / 100;
        // O resto vai para o primeiro membro (quem pagou)
        double remainder = Math.round((amount - share * count) * 100.0) / 100.0;

        List<Expense.Split> splits = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Expense.Split split = new Expense.Split();
            split.setUserId(memberIds.get(i));
            split.setAmount(i == 0 ? share + remainder : share);
            splits.add(split);
        }
        return splits;
    }

    // Valida se os splits somam o total da despesa
    private void validateSplits(double amount, List<Expense.Split> splits) {
        if (splits == null || splits.isEmpty()) {
            throw new ValidationException("Os splits são obrigatórios");
        }
        double total = splits.stream().mapToDouble(Expense.Split::getAmount).sum();
        if (Math.abs(total - amount) > 0.01) {
            throw new ValidationException("Os splits devem somar o valor total da despesa");
        }
    }
}