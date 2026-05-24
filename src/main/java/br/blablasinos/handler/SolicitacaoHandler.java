package br.blablasinos.handler;

import br.blablasinos.model.Reserva;
import br.blablasinos.repository.CaronaRepository;
import br.blablasinos.repository.SqliteReservaRepository;
import br.blablasinos.repository.SqliteUsuarioRepository;
import br.blablasinos.service.CaronaService;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class SolicitacaoHandler implements HttpHandler {

    private final CaronaService caronaService;
    private final Gson gson;

    public SolicitacaoHandler() {
        this.caronaService = new CaronaService(
            new CaronaRepository("jdbc:sqlite:caronas.db"),
            new SqliteUsuarioRepository(),
            new SqliteReservaRepository()
        );
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            if ("POST".equals(method)) {
                handlePost(exchange);
            } else if ("DELETE".equals(method)) {
                handleDelete(exchange);
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Metodo nao permitido\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void handlePost(HttpExchange exchange) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            SolicitacaoRequest request = gson.fromJson(reader, SolicitacaoRequest.class);
            Reserva reservaCriada = caronaService.solicitarVaga(request.getPassageiroId(), request.getCaronaId());
            sendResponse(exchange, 201, gson.toJson(reservaCriada));
        }
    }

    private void handleDelete(HttpExchange exchange) throws Exception {
        String query = exchange.getRequestURI().getQuery();
        long reservaId = Long.parseLong(getParameter(query, "reservaId"));
        long passageiroId = Long.parseLong(getParameter(query, "passageiroId"));
        caronaService.cancelarSolicitacao(passageiroId, reservaId);
        sendResponse(exchange, 200, "{\"message\": \"Solicitacao cancelada com sucesso!\"}");
    }

    private String getParameter(String query, String paramName) {
        if (query == null) throw new IllegalArgumentException("Query string vazia.");
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1 && pair[0].equals(paramName)) return pair[1];
        }
        throw new IllegalArgumentException("Parametro obrigatorio nao encontrado: " + paramName);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static class SolicitacaoRequest {
        private Long passageiroId;
        private Long caronaId;
        public Long getPassageiroId() { return passageiroId; }
        public Long getCaronaId() { return caronaId; }
    }
}