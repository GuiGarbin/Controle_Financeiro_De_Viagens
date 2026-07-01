package org.example.config;

import org.springframework.beans.factory.annotation.Value; // para injetar o valor de uma propriedade de configuração (app.data-dir)
import org.springframework.boot.ApplicationArguments; // para acessar os argumentos de inicialização da aplicação
import org.springframework.boot.ApplicationRunner; // interface que permite executar código após a inicialização do Spring Boot
import org.springframework.stereotype.Component; // para marcar esta classe como um componente do Spring, que será detectado e gerenciado automaticamente

import java.io.IOException; // para lidar com possíveis erros de entrada/saída ao criar arquivos
import java.nio.file.*; // para trabalhar com arquivos e diretórios de forma segura e portátil (Files, Path, Paths)

@Component // Indica que esta classe é um componente do Spring, que será detectado e gerenciado automaticamente
public class AppInitializer implements ApplicationRunner {

    // Caminho da pasta de dados vem da propriedade app.data-dir (a mesma usada
    // pelo JsonStorageConfig), garantindo que inicialização e persistência
    // apontem para o mesmo lugar.
    private final Path dataDir;

    public AppInitializer(@Value("${app.data-dir}") String dataDir) {
        this.dataDir = Paths.get(dataDir);
    }

    private static final String[] JSON_FILES = {
        "users.json",
        "trips.json",
        "expenses.json",
        "categories.json",
        "settlements.json"
    };

    @Override // Este método será executado automaticamente após a inicialização do Spring Boot
    public void run(ApplicationArguments args) throws IOException {
        // Cria a pasta /data se não existir
        Files.createDirectories(dataDir);

        // Para cada arquivo, cria vazio se não existir
        for (String fileName : JSON_FILES) {
            Path filePath = dataDir.resolve(fileName);
            if (!Files.exists(filePath)) {
                Files.writeString(filePath, "[]");
                System.out.println("Arquivo criado: " + fileName);
            }
        }

        System.out.println("✅ Arquivos JSON verificados e prontos.");
    }
}