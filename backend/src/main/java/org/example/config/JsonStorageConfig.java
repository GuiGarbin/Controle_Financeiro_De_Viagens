package org.example.config;

import com.fasterxml.jackson.annotation.JsonInclude; // para ignorar campos null no JSON
import com.fasterxml.jackson.databind.ObjectMapper; // classe principal do Jackson para conversão Java <-> JSON
import com.fasterxml.jackson.databind.SerializationFeature; // para configurar o formato do JSON (ex: indentado)
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import org.example.model.*; //
import org.example.repository.*; // importa as classes de repositório (UserRepository, TripRepository, etc.)
import org.springframework.beans.factory.annotation.Value; // para injetar o valor da propriedade app.data-dir
import org.springframework.context.annotation.Bean; // para registrar beans no contexto do Spring
import org.springframework.context.annotation.Configuration; // para marcar esta classe como uma classe de configuração do Spring

import java.nio.file.Path; // para representar caminhos de arquivos de forma segura e portátil
import java.nio.file.Paths; // para criar objetos Path a partir de strings de caminho de arquivo
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration // Indica que esta classe é uma classe de configuração do Spring, onde podemos definir beans e configurações
public class JsonStorageConfig {

    // Pasta onde ficam os arquivos JSON. Vem da propriedade app.data-dir,
    // a mesma usada pelo AppInitializer, para não divergirem (causa do HTTP 500).
    // O valor padrão abaixo só vale quando a classe é instanciada via `new`
    // (ex.: TripController, fora do Spring); quando gerenciada pelo Spring, o
    // @Value sobrescreve com a propriedade configurada.
    @Value("${app.data-dir}")
    private String dataDir = "backend/src/main/resources/data";

    private Path dataDir() {
        return Paths.get(dataDir);
    }

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
        return new UserRepository(dataDir().resolve("users.json"), objectMapper);
    }

    @Bean
    public TripRepository tripRepository(ObjectMapper objectMapper) {
        return new TripRepository(dataDir().resolve("trips.json"), objectMapper);
    }

    @Bean
    public ExpenseRepository expenseRepository(ObjectMapper objectMapper) {
        return new ExpenseRepository(dataDir().resolve("expenses.json"), objectMapper);
    }

    @Bean
    public CategoryRepository categoryRepository(ObjectMapper objectMapper) {
        return new CategoryRepository(dataDir().resolve("categories.json"), objectMapper);
    }

    @Bean
    public SettlementRepository settlementRepository(ObjectMapper objectMapper) {
        return new SettlementRepository(dataDir().resolve("settlements.json"), objectMapper);
    }
}