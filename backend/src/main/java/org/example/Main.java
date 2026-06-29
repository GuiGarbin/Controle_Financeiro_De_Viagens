package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // habilita a atualização periódica do câmbio (ExchangeRateService)
public class Main {
    public static void main (String[] args){
        // Inicia a aplicação como servidor web Spring Boot (porta 8080),
        // que é o que o frontend Electron espera para fazer as chamadas HTTP.
        SpringApplication.run(Main.class, args);

        // Modo console (menu via terminal) — desativado enquanto rodamos como servidor web.
        // Para voltar ao menu de terminal, comente o SpringApplication.run acima e
        // descomente as duas linhas abaixo:
        // Menu menu = new Menu();
        // menu.mainScreen();
    }
}

