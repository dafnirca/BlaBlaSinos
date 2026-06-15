package br.blablasinos;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import br.blablasinos.handler.CadastroHandler;
import br.blablasinos.handler.CaronaHandler;
import br.blablasinos.handler.LoginHandler;
import br.blablasinos.handler.NotificacaoHandler;
import br.blablasinos.handler.PerfilHandler;
import br.blablasinos.handler.SolicitacaoHandler;
import br.blablasinos.handler.StaticFileHandler;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/api/cadastro", new CadastroHandler());
        server.createContext("/api/login", new LoginHandler());
        server.createContext("/api/perfil", new PerfilHandler());
        server.createContext("/api/avaliacoes", new br.blablasinos.handler.AvaliacaoHandler());
        server.createContext("/api/caronas", new CaronaHandler()); 
        server.createContext("/api/solicitacoes", new SolicitacaoHandler());
        server.createContext("/api/notificacoes", new NotificacaoHandler());

        server.createContext("/", new StaticFileHandler());

        server.setExecutor(null);
        server.start();
        
        System.out.println("BlaBlaSinos iniciado!");
        System.out.println("Servidor escutando na porta " + port);
        System.out.println("Acesse: http://localhost:" + port + "/login.html");
        // Server started
    }
}
