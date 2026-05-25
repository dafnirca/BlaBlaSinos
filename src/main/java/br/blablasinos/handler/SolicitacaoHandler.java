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
import java.util.List;

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
            } else if ("GET".equals(method)) {
                handleGet(exchange);
            } else if ("PUT".equals(method)) {
                handlePut(exchange);
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

    private void handleGet(HttpExchange exchange) throws Exception {
        String query = exchange.getRequestURI().getQuery();
        List<Reserva> reservas;
        
        try {
            // Tenta obter motoristaId para listar solicitações pendentes
            String motoristaIdStr = getParameterOptional(query, "motoristaId");
            if (motoristaIdStr != null) {
                long motoristaId = Long.parseLong(motoristaIdStr);
                reservas = caronaService.listarSolicitacoesPendentes(motoristaId);
            } else {
                // Tenta obter passageiroId para listar solicitações do passageiro
                String passageiroIdStr = getParameterOptional(query, "passageiroId");
                if (passageiroIdStr != null) {
                    long passageiroId = Long.parseLong(passageiroIdStr);
                    reservas = caronaService.listarSolicitacoesDoPassageiro(passageiroId);
                } else {
                    throw new IllegalArgumentException("Forneça motoristaId ou passageiroId como parâmetro.");
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("IDs devem ser números válidos.");
        }
        
        sendResponse(exchange, 200, gson.toJson(reservas));
    }

    private void handlePut(HttpExchange exchange) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            DecisaoRequest request = gson.fromJson(reader, DecisaoRequest.class);
            if (request == null || request.getMotoristaId() == null || request.getReservaId() == null || request.getAcao() == null) {
                throw new IllegalArgumentException("motoristaId, reservaId e acao sao obrigatorios.");
            }

            boolean aceitar;
            if ("ACEITAR".equalsIgnoreCase(request.getAcao())) {
                aceitar = true;
            } else if ("RECUSAR".equalsIgnoreCase(request.getAcao()) || "REJEITAR".equalsIgnoreCase(request.getAcao())) {
                aceitar = false;
            } else {
                throw new IllegalArgumentException("Acao invalida. Use ACEITAR ou RECUSAR.");
            }

            Reserva reservaAtualizada = caronaService.decidirSolicitacao(request.getMotoristaId(), request.getReservaId(), aceitar);
            sendResponse(exchange, 200, gson.toJson(reservaAtualizada));
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

    private String getParameterOptional(String query, String paramName) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1 && pair[0].equals(paramName)) return pair[1];
        }
        return null;
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

    private static class DecisaoRequest {
        private Long motoristaId;
        private Long reservaId;
        private String acao;
        public Long getMotoristaId() { return motoristaId; }
        public Long getReservaId() { return reservaId; }
        public String getAcao() { return acao; }
    }
}