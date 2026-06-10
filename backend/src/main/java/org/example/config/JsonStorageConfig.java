package org.example.config;

import com.fasterxml.jackson.annotation.JsonInclude; // para ignorar campos null no JSON
import com.fasterxml.jackson.databind.ObjectMapper; // classe principal do Jackson para conversão Java <-> JSON
import com.fasterxml.jackson.databind.SerializationFeature; // para configurar o formato do JSON (ex: indentado)
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.example.model.*; //
import org.example.repository.*; // importa as classes de repositório (UserRepository, TripRepository, etc.)
import org.springframework.context.annotation.Bean; // para registrar beans no contexto do Spring
import org.springframework.context.annotation.Configuration; // para marcar esta classe como uma classe de configuração do Spring

import java.nio.file.Path; // para representar caminhos de arquivos de forma segura e portátil
import java.nio.file.Paths; // para criar objetos Path a partir de strings de caminho de arquivo
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration // Indica que esta classe é uma classe de configuração do Spring, onde podemos definir beans e configurações
public class JsonStorageConfig {

    // Pasta onde ficam os arquivos JSON
    private static final Path DATA_DIR = Paths.get("backend/src/main/resources/data");

    // Configura o Jackson (conversor Java <-> JSON)
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        DateTimeFormatter molde = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        JavaTimeModule newTime = new JavaTimeModule();
        newTime.addSerializer(LocalDate.class, new LocalDateSerializer(molde));
        newTime.addDeserializer(LocalDate.class, new LocalDateDeserializer(molde));
        mapper.registerModule(newTime);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }

    // Cria e registra cada repository com seu arquivo correspondente
    @Bean
    public UserRepository userRepository(ObjectMapper objectMapper) {
        return new UserRepository(DATA_DIR.resolve("users.json"), objectMapper);
    }

    @Bean
    public TripRepository tripRepository(ObjectMapper objectMapper) {
        return new TripRepository(DATA_DIR.resolve("trips.json"), objectMapper);
    }

    @Bean
    public ExpenseRepository expenseRepository(ObjectMapper objectMapper) {
        return new ExpenseRepository(DATA_DIR.resolve("expenses.json"), objectMapper);
    }

    @Bean
    public CategoryRepository categoryRepository(ObjectMapper objectMapper) {
        return new CategoryRepository(DATA_DIR.resolve("categories.json"), objectMapper);
    }

    @Bean
    public SettlementRepository settlementRepository(ObjectMapper objectMapper) {
        return new SettlementRepository(DATA_DIR.resolve("settlements.json"), objectMapper);
    }
}