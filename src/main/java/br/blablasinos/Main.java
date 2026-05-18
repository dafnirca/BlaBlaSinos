package br.blablasinos;

import br.blablasinos.handler.CadastroHandler;
import br.blablasinos.handler.LoginHandler;
import br.blablasinos.handler.PerfilHandler;
import br.blablasinos.handler.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/cadastro", new CadastroHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/perfil", new PerfilHandler());
        server.setExecutor(null); // Usa o executor padrão
        server.start();

        System.out.println("BlaBlaSinos iniciado!");
        System.out.println("Servidor escutando na porta " + port);
        System.out.println("Acesse: http://localhost:" + port + "/login.html");
    }
}