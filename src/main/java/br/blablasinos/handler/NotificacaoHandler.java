package br.blablasinos.handler;

import br.blablasinos.model.Notificacao;
import br.blablasinos.service.NotificacaoService;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NotificacaoHandler implements HttpHandler {

    private final NotificacaoService notificacaoService;
    private final Gson gson;

    public NotificacaoHandler() {
        this.notificacaoService = new NotificacaoService();
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            if ("GET".equals(method)) {
                handleGet(exchange);
            } else if ("PUT".equals(method)) {
                handlePut(exchange);
            } else {
                sendResponse(exchange, 405, "{\"error\":\"Metodo nao permitido\"}");
            }
        } catch (Exception e) {
            sendResponse(exchange, 400, "{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        long usuarioId = Long.parseLong(getParameter(query, "usuarioId"));
        List<Notificacao> notificacoes = notificacaoService.listarPorUsuario(usuarioId);
        sendResponse(exchange, 200, gson.toJson(notificacoes));
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            MarcarLidaRequest request = gson.fromJson(reader, MarcarLidaRequest.class);
            if (request == null || request.usuarioId == null || request.notificacaoId == null) {
                throw new IllegalArgumentException("usuarioId e notificacaoId sao obrigatorios.");
            }
            notificacaoService.marcarComoLida(request.usuarioId, request.notificacaoId);
            sendResponse(exchange, 200, "{\"message\":\"Notificacao marcada como lida.\"}");
        }
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

    private static class MarcarLidaRequest {
        private Long usuarioId;
        private Long notificacaoId;
    }
}
