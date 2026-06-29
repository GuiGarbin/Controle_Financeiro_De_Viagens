package org.example.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

// Tabela de câmbio (base BRL) mantida em memória e persistida em disco (rates.json).
// - Na inicialização: lê o disco (se houver) e tenta baixar TODAS as taxas de uma
//   vez (uma só requisição base=BRL traz todas as moedas).
// - Online: salva/atualiza o disco. Offline: usa o que estiver salvo.
// - Atualiza de tempos em tempos; se voltar a ter internet, as taxas são renovadas.
// Assim as conversões (criar viagem, adicionar gasto) não dependem de internet a cada uso.
@Service
public class ExchangeRateService {

    private static final String BASE = "BRL";
    private static final String URL = "https://api.frankfurter.dev/v1/latest?base=" + BASE;

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path ratesFile;

    // moeda -> quantas unidades dela valem 1 BRL (formato da API Frankfurter, base=BRL)
    private volatile Map<String, Double> rates = new HashMap<>();
    private volatile String date = null;

    public ExchangeRateService(@Value("${app.data-dir}") String dataDir) {
        this.ratesFile = Paths.get(dataDir).resolve("rates.json");
        loadFromDisk(); // taxas ficam disponíveis já na subida, mesmo offline
    }

    // Primeira atualização logo após a aplicação subir (não bloqueia a subida).
    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        refresh();
    }

    // Renova de hora em hora (no-op silencioso quando offline).
    @Scheduled(fixedDelay = 3_600_000L, initialDelay = 3_600_000L)
    public void scheduledRefresh() {
        refresh();
    }

    // Câmbio: quantos BRL valem 1 unidade de `currency`.
    public double getRate(String currency) {
        if (currency == null || !currency.matches("[A-Za-z]{3}")) {
            throw new ValidationException("Moeda invalida: " + currency);
        }
        String code = currency.toUpperCase();
        if (code.equals(BASE)) return 1.0;
        Double perBrl = rates.get(code);
        if (perBrl == null || perBrl == 0.0) {
            throw new ValidationException("Câmbio indisponível para " + code
                    + (rates.isEmpty() ? " (sem conexão e sem dados salvos)." : "."));
        }
        return 1.0 / perBrl; // BRL por 1 unidade da moeda
    }

    private synchronized void refresh() {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .timeout(Duration.ofSeconds(8))
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(resp.body());
            JsonNode ratesNode = root.get("rates");
            if (ratesNode == null) return; // resposta inesperada: mantém o que já tinha
            Map<String, Double> fresh = new HashMap<>();
            ratesNode.fields().forEachRemaining(e -> fresh.put(e.getKey().toUpperCase(), e.getValue().asDouble()));
            fresh.put(BASE, 1.0);
            this.rates = fresh;
            this.date = root.has("date") ? root.get("date").asText() : null;
            saveToDisk();
        } catch (Exception e) {
            // Offline ou erro: mantém os câmbios atuais (memória/disco).
            System.out.println("[ExchangeRateService] não foi possível atualizar o câmbio: " + e.getMessage());
        }
    }

    private void loadFromDisk() {
        try {
            if (!Files.exists(ratesFile)) return;
            JsonNode root = mapper.readTree(Files.readAllBytes(ratesFile));
            JsonNode ratesNode = root.get("rates");
            if (ratesNode == null) return;
            Map<String, Double> loaded = new HashMap<>();
            ratesNode.fields().forEachRemaining(e -> loaded.put(e.getKey().toUpperCase(), e.getValue().asDouble()));
            loaded.put(BASE, 1.0);
            this.rates = loaded;
            this.date = root.has("date") ? root.get("date").asText() : null;
        } catch (Exception e) {
            System.out.println("[ExchangeRateService] falha ao ler rates.json: " + e.getMessage());
        }
    }

    private void saveToDisk() {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put("base", BASE);
            if (date != null) root.put("date", date);
            ObjectNode r = root.putObject("rates");
            rates.forEach((k, v) -> r.put(k, v));
            Files.createDirectories(ratesFile.getParent());
            Path tmp = ratesFile.resolveSibling("rates.json.tmp");
            mapper.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), root);
            Files.move(tmp, ratesFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            System.out.println("[ExchangeRateService] falha ao salvar rates.json: " + e.getMessage());
        }
    }
}
