package org.example.repository;

import com.fasterxml.jackson.core.type.TypeReference; // Importação necessária para usar TypeReference com generics
import com.fasterxml.jackson.databind.ObjectMapper; // biblioteca para converter entre objetos Java e JSON
import org.example.model.Expense; // Importação do modelo Expense, que é a entidade que este repositório irá gerenciar

import java.nio.file.Path; // para manipulação de arquivos (Paths, Files, StandardCopyOption)
import java.util.List; // para trabalhar com listas de objetos

public class ExpenseRepository extends JsonRepository<Expense> {

    public ExpenseRepository(Path filePath, ObjectMapper objectMapper) {
        super(filePath, objectMapper, new TypeReference<List<Expense>>() {});
    }

    // Busca todas as despesas de uma viagem específica
    public List<Expense> findByTripId(String tripId) {
        return findAllByField(Expense::getTripId, tripId);
    }
}