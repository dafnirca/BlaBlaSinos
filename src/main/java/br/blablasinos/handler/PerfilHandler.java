package br.blablasinos.handler;

import br.blablasinos.model.Usuario;
import br.blablasinos.service.UsuarioService;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class PerfilHandler implements HttpHandler {

    private final UsuarioService usuarioService;
    private final Gson gson;

    public PerfilHandler() {
        this.usuarioService = new UsuarioService();
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Usaremos o método PUT para atualizações
        if ("PUT".equals(exchange.getRequestMethod())) {
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

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}