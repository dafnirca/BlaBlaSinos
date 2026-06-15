package br.blablasinos.handler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import br.blablasinos.model.Usuario;
import br.blablasinos.service.UsuarioService;

public class PerfilHandler implements HttpHandler {

    private final UsuarioService usuarioService;
    private final Gson gson;

    public PerfilHandler() {
        this.usuarioService = new UsuarioService();
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if ("GET".equalsIgnoreCase(method)) {
            try {
                String query = exchange.getRequestURI().getQuery();
                Long userId = parseIdFromQuery(query);
                if (userId == null) {
                    sendResponse(exchange, 400, "{\"error\": \"ID de usuário ausente ou inválido\"}");
                    return;
                }

                Usuario usuario = usuarioService.buscarPorId(userId)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

                ProfileResponse profileResponse = new ProfileResponse(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getCnh(),
                    usuario.getMarcaVeiculo(),
                    usuario.getModeloVeiculo(),
                    usuario.getCorVeiculo(),
                    usuario.getPlacaVeiculo(),
                    usuario.getVagas()
                );

                String response = gson.toJson(profileResponse);
                sendResponse(exchange, 200, response);
            } catch (Exception e) {
                String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
                sendResponse(exchange, 400, errorResponse);
            }
        } else if ("PUT".equalsIgnoreCase(method)) {
            try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
                Usuario usuario = gson.fromJson(reader, Usuario.class);
                
                // IMPORTANTE: O ID do usuário logado deveria vir de uma sessão segura.
                // Por simplicidade agora, vamos assumir que o frontend o envia.
                usuarioService.atualizarPerfil(usuario);

                String response = "{\"message\": \"Perfil atualizado com sucesso!\"}";
                sendResponse(exchange, 200, response);
            } catch (Exception e) {
                String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
                sendResponse(exchange, 400, errorResponse); // 400 = Bad Request
            }
        } else {
            sendResponse(exchange, 405, "{\"error\": \"Metodo nao permitido\"}"); // 405 = Method Not Allowed
        }

    }

    private Long parseIdFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String param : query.split("&")) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2 && "id".equalsIgnoreCase(parts[0])) {
                try {
                    return Long.parseLong(parts[1]);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }

        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static class ProfileResponse {
        private final Long id;
        private final String nome;
        private final String email;
        private final String cnh;
        private final String marcaVeiculo;
        private final String modeloVeiculo;
        private final String corVeiculo;
        private final String placaVeiculo;
        private final Integer vagas;

        public ProfileResponse(Long id, String nome, String email, String cnh, String marcaVeiculo, String modeloVeiculo, String corVeiculo, String placaVeiculo, Integer vagas) {
            this.id = id;
            this.nome = nome;
            this.email = email;
            this.cnh = cnh;
            this.marcaVeiculo = marcaVeiculo;
            this.modeloVeiculo = modeloVeiculo;
            this.corVeiculo = corVeiculo;
            this.placaVeiculo = placaVeiculo;
            this.vagas = vagas;
        }

        public Long getId() { return id; }
        public String getNome() { return nome; }
        public String getEmail() { return email; }
        public String getCnh() { return cnh; }
        public String getMarcaVeiculo() { return marcaVeiculo; }
        public String getModeloVeiculo() { return modeloVeiculo; }
        public String getCorVeiculo() { return corVeiculo; }
        public String getPlacaVeiculo() { return placaVeiculo; }
        public Integer getVagas() { return vagas; }
    }
}