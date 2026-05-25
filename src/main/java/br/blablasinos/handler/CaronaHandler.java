package br.blablasinos.handler;

import br.blablasinos.model.Carona;
import br.blablasinos.service.CaronaService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CaronaHandler implements HttpHandler {

    private final CaronaService caronaService;
    private final Gson gson;

    public CaronaHandler() {
        this.caronaService = new CaronaService();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            // Roteamento baseado no caminho e no método
            if ("/api/caronas/buscar".equals(path) && "GET".equals(method)) {
                handleSearch(exchange); // Rota para busca de passageiros
            } else if ("/api/caronas".equals(path) && "GET".equals(method)) {
                handleGetMinhasCaronas(exchange); // Rota para listar caronas do motorista
            } else if ("/api/caronas".equals(path) && "POST".equals(method)) {
                handlePost(exchange); // Rota para criar uma nova carona
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Rota nao encontrada\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // NOVO MÉTODO: Lida com a busca de caronas por passageiros
    private void handleSearch(HttpExchange exchange) throws Exception {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        
        String destino = params.get("destino");
        String data = params.get("data");

        List<Carona> caronas = caronaService.buscarCaronasDisponiveis(destino, data);
        sendResponse(exchange, 200, gson.toJson(caronas));
    }

    // MÉTODO RENOMEADO: Lida com a listagem de caronas de um motorista
    private void handleGetMinhasCaronas(HttpExchange exchange) throws Exception {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        // Se 'id' for fornecido, retorna a carona específica
        if (params.containsKey("id") && params.get("id") != null && !params.get("id").isBlank()) {
            long id = Long.parseLong(params.get("id"));
            Carona carona = caronaService.buscarCaronaPorId(id);
            sendResponse(exchange, 200, gson.toJson(carona));
            return;
        }

        long motoristaId = Long.parseLong(params.get("motoristaId"));
        List<Carona> caronas = caronaService.listarMinhasCaronas(motoristaId);
        sendResponse(exchange, 200, gson.toJson(caronas));
    }

    // Lida com a criação de uma nova carona
    private void handlePost(HttpExchange exchange) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Carona carona = gson.fromJson(reader, Carona.class);
            Carona caronaCadastrada = caronaService.cadastrarCarona(
                carona.getMotoristaId(),
                carona.getOrigem(),
                carona.getDestino(),
                carona.getDataHora(),
                carona.getVagasTotais()
            );
            sendResponse(exchange, 201, gson.toJson(caronaCadastrada));
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    // Método auxiliar para extrair parâmetros da URL
    private Map<String, String> parseQuery(String query) {
        if (query == null) {
            return Map.of();
        }
        return Stream.of(query.split("&"))
            .map(s -> s.split("=", 2))
            .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
    }
}