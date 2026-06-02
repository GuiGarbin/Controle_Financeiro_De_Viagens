package org.example.repository;

import com.fasterxml.jackson.core.type.TypeReference; // Importação necessária para usar TypeReference com generics
import com.fasterxml.jackson.databind.ObjectMapper; // biblioteca para converter entre objetos Java e JSON
import org.example.model.Category; // Importação do modelo Category, que é a entidade que este repositório irá gerenciar

import java.nio.file.Path;
import java.util.List;

public class CategoryRepository extends org.example.repository.JsonRepository<Category> {

    public CategoryRepository(Path filePath, ObjectMapper objectMapper) {
        super(filePath, objectMapper, new TypeReference<List<Category>>() {});
    }
}