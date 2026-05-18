package br.blablasinos.handler;

import br.blablasinos.model.TipoUsuario;
import br.blablasinos.model.Usuario;
import br.blablasinos.service.UsuarioService;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CadastroHandler implements HttpHandler {

    private final UsuarioService usuarioService;
    private final Gson gson;

    public CadastroHandler() {
        this.usuarioService = new UsuarioService();
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\": \"Metodo nao permitido\"}");
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8)) {
            Usuario usuario = gson.fromJson(reader, Usuario.class);

            if (usuario.getTipo() == null) {
                usuario.setTipo(TipoUsuario.PASSAGEIRO);
            }

            Usuario cadastrado = usuarioService.cadastrar(usuario);

            String response = gson.toJson(new CadastroResponse(
                cadastrado.getId(),
                cadastrado.getNome(),
                cadastrado.getEmail(),
                cadastrado.getTipo().name(),
                "Cadastro realizado com sucesso!"
            ));

            sendResponse(exchange, 201, response);
        } catch (Exception e) {
            String errorResponse = gson.toJson(new ErrorResponse(e.getMessage()));
            sendResponse(exchange, 400, errorResponse);
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static class CadastroResponse {
        private final Long id;
        private final String nome;
        private final String email;
        private final String tipo;
        private final String message;

        CadastroResponse(Long id, String nome, String email, String tipo, String message) {
            this.id = id;
            this.nome = nome;
            this.email = email;
            this.tipo = tipo;
            this.message = message;
        }
    }

    private static class ErrorResponse {
        private final String error;

        ErrorResponse(String error) {
            this.error = error;
        }
    }
}
