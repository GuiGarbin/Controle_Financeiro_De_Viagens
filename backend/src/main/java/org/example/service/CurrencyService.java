package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exception.ValidationException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Gerenciado pelo Spring (@Service) para poder ser injetado no TripService.
// Continua funcionando via `new` no fluxo de terminal (TripController), pois o
// construtor sem argumentos foi mantido.
@Service
public class CurrencyService {

    private final HttpClient client;
    private final ObjectMapper motor;

    public CurrencyService() {
        this.client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.motor = new ObjectMapper();
    }

    public double getRate(String origin, String destiny){
        // Validacao basica do codigo da moeda (3 letras, ISO 4217).
        if (origin == null || !origin.matches("[A-Za-z]{3}")) {
            throw new ValidationException("Moeda invalida: " + origin);
        }

        // Atalho: moeda de origem igual a de destino dispensa chamada externa
        // (a API Frankfurter, inclusive, nao aceita base == symbol).
        if (origin.equalsIgnoreCase(destiny)) {
            return 1.0;
        }

        String url = String.format("https://api.frankfurter.dev/v1/latest?base=%s&symbols=%s", origin, destiny);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode treeJson = motor.readTree(response.body());
            JsonNode rates = treeJson.get("rates");
            if (rates == null || rates.get(destiny) == null) {
                throw new ValidationException("Nao foi possivel obter o cambio para " + origin);
            }
            return rates.get(destiny).asDouble();
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            // Falha de rede / parsing: transforma em erro de validacao (HTTP 400)
            // em vez de derrubar a request com 500.
            throw new ValidationException("Falha ao consultar o cambio (sem conexao?). Tente novamente.");
        }
    }
}
