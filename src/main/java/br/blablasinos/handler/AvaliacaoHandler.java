package br.blablasinos.handler;

import br.blablasinos.model.Avaliacao;
import br.blablasinos.service.AvaliacaoService;
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

public class AvaliacaoHandler implements HttpHandler {

    private final AvaliacaoService avaliacaoService;
    private final Gson gson;

    public AvaliacaoHandler() {
        this.avaliacaoService = new AvaliacaoService();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if ("POST".equals(method)) {
                handlePost(exchange);
            } else if ("GET".equals(method)) {
                handleGet(exchange);
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Metodo nao permitido\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, gson.toJson(Map.of("error", e.getMessage())));
        }
    }

    private void handlePost(HttpExchange exchange) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            AvaliacaoRequest request = gson.fromJson(reader, AvaliacaoRequest.class);
            Avaliacao avaliacao = avaliacaoService.avaliar(
                request.caronaId,
                request.avaliadorId,
                request.avaliadoId,
                request.nota,
                request.comentario
            );
            sendResponse(exchange, 201, gson.toJson(avaliacao));
        }
    }

    private void handleGet(HttpExchange exchange) throws Exception {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());

        if (params.containsKey("caronaId") && params.containsKey("avaliadorId")) {
            Long caronaId = Long.parseLong(params.get("caronaId"));
            Long avaliadorId = Long.parseLong(params.get("avaliadorId"));
            List<Long> avaliaveis = avaliacaoService.listarAvaliaveis(caronaId, avaliadorId);
            sendResponse(exchange, 200, gson.toJson(Map.of("avaliaveis", avaliaveis)));
            return;
        }

        if (params.containsKey("usuarioId")) {
            Long usuarioId = Long.parseLong(params.get("usuarioId"));
            sendResponse(exchange, 200, gson.toJson(avaliacaoService.resumoRecebidas(usuarioId)));
            return;
        }

        throw new IllegalArgumentException("Informe usuarioId ou caronaId e avaliadorId.");
    }

    private Map<String, String> parseQuery(String query) {
        if (query == null || query.isBlank()) return Map.of();
        return Stream.of(query.split("&"))
            .map(s -> s.split("=", 2))
            .collect(Collectors.toMap(a -> a[0], a -> a.length > 1 ? a[1] : ""));
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static class AvaliacaoRequest {
        private Long caronaId;
        private Long avaliadorId;
        private Long avaliadoId;
        private int nota;
        private String comentario;
    }
}
