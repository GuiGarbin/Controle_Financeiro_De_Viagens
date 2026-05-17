package org.example.repository;

import com.fasterxml.jackson.core.type.TypeReference; // classe para manter informações de tipo genérico em tempo de execução
import com.fasterxml.jackson.databind.ObjectMapper; // biblioteca para converter entre objetos Java e JSON
import org.example.model.BaseEntity;

import java.io.IOException; // para tratar erros de leitura/escrita de arquivos
import java.nio.file.*; // para manipulação de arquivos (Paths, Files, StandardCopyOption)
import java.util.ArrayList; // para criar uma lista vazia caso o arquivo JSON não exista ou esteja vazio
import java.util.List; // para trabalhar com listas de objetos
import java.util.Optional; // para retornar um resultado que pode ou não conter um valor (ex: findById pode não encontrar o registro)
import java.util.concurrent.locks.ReentrantReadWriteLock; // para controlar o acesso concorrente ao arquivo JSON, permitindo múltiplas leituras mas apenas uma escrita
import java.util.function.Function; // para passar uma função que extrai um campo específico de um objeto (ex: Settlement::getTripId) ao buscar por esse campo

public class JsonRepository<T extends BaseEntity> {

    private final Path filePath; // caminho do arquivo JSON onde os dados serão armazenados
    private final ObjectMapper objectMapper; // biblioteca para converter entre objetos Java e JSON
    private final TypeReference<List<T>> typeReference; // referência de tipo para desserialização (ex: new TypeReference<List<Expense>>() {})
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(); // lock que controla quem pode ler/escrever o arquivo

    public JsonRepository(Path filePath, ObjectMapper objectMapper, TypeReference<List<T>> typeReference) {
        this.filePath = filePath;
        this.objectMapper = objectMapper;
        this.typeReference = typeReference;
    }

    // Retorna todos os registros do arquivo JSON
    public List<T> findAll() {
        lock.readLock().lock();
        try {
            return readFile();
        } finally {
            lock.readLock().unlock();
        }
    }

    // Busca um registro pelo id
    public Optional<T> findById(String id) {
        return findAll().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }

    // Salva um registro (insere se não existe, atualiza se já existe)
    public T save(T entity) {
        lock.writeLock().lock();
        try {
            List<T> entities = readFile();
            boolean found = false;

            for (int i = 0; i < entities.size(); i++) {
                if (entities.get(i).getId().equals(entity.getId())) {
                    entities.set(i, entity); // atualiza
                    found = true;
                    break;
                }
            }

            if (!found) {
                entities.add(entity); // insere
            }

            writeFile(entities);
            return entity;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Remove um registro pelo id
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            List<T> entities = readFile();
            entities.removeIf(e -> e.getId().equals(id));
            writeFile(entities);
        } finally {
            lock.writeLock().unlock();
        }
    }

    // Busca registros por qualquer campo (ex: todos expenses de um tripId)
    public List<T> findAllByField(Function<T, String> getter, String value) {
        return findAll().stream()
                .filter(e -> value.equals(getter.apply(e)))
                .toList();
    }

    // Lê o arquivo JSON e converte para lista de objetos Java
    private List<T> readFile() {
        try {
            if (!Files.exists(filePath)) return new ArrayList<>();
            byte[] bytes = Files.readAllBytes(filePath);
            if (bytes.length == 0) return new ArrayList<>();
            return objectMapper.readValue(bytes, typeReference);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo: " + filePath, e);
        }
    }

    // Converte a lista para JSON e salva no arquivo (escrita atômica)
    private void writeFile(List<T> entities) {
        try {
            Path tmp = filePath.resolveSibling(filePath.getFileName() + ".tmp");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), entities);
            Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao escrever arquivo: " + filePath, e);
        }
    }
}