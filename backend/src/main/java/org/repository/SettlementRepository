package org.example.repository;

import com.fasterxml.jackson.core.type.TypeReference; // Importação necessária para usar TypeReference com generics
import com.fasterxml.jackson.databind.ObjectMapper; // biblioteca para converter entre objetos Java e JSON
import org.example.model.Settlement; // Importação do modelo Settlement, que é a entidade que este repositório irá gerenciar

import java.nio.file.Path;
import java.util.List;

public class SettlementRepository extends JsonRepository<Settlement> {

    public SettlementRepository(Path filePath, ObjectMapper objectMapper) {
        super(filePath, objectMapper, new TypeReference<List<Settlement>>() {});
    }

    // Busca todos os acertos de uma viagem específica
    public List<Settlement> findByTripId(String tripId) {
        return findAllByField(Settlement::getTripId, tripId);
    }
}