package org.example.repository;

import com.fasterxml.jackson.core.type.TypeReference; // Importação necessária para usar TypeReference com generics
import com.fasterxml.jackson.databind.ObjectMapper; // biblioteca para converter entre objetos Java e JSON
import org.example.users.User; // Importação do modelo User, que é a entidade que este repositório irá gerenciar

import java.nio.file.Path; // para manipulação de arquivos (Paths, Files, StandardCopyOption)
import java.util.List; // para trabalhar com listas de objetos
import java.util.Optional; // para retornar um resultado que pode ou não conter um valor (ex: findByEmail pode não encontrar o registro)

public class UserRepository extends JsonRepository<User> {

    public UserRepository(Path filePath, ObjectMapper objectMapper) {
        super(filePath, objectMapper, new TypeReference<List<User>>() {});
    }

    // Método para buscar um usuário pelo email, retornando um Optional que pode ou não conter um User
    public Optional<User> findByEmail(String email) { 
        return findAll().stream()
                .filter(u -> email.equals(u.getEmail()))
                .findFirst();
    }
}