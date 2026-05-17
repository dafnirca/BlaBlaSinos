package br.blablasinos.repository;

import br.blablasinos.model.TipoUsuario;
import br.blablasinos.model.Usuario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class SqliteUsuarioRepository implements UsuarioRepository {

    private static final String DEFAULT_URL = "jdbc:sqlite:caronas.db";
    private final String databaseUrl;

    public SqliteUsuarioRepository() {
        this(DEFAULT_URL);
    }

    public SqliteUsuarioRepository(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        criarTabelaSeNecessario();
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }

        String sql = "SELECT 1 FROM usuarios WHERE lower(email) = lower(?) LIMIT 1";

        try (Connection connection = criarConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Falha ao consultar usuário por e-mail.", exception);
        }
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios(nome, email, senha, tipo) VALUES(?, ?, ?, ?)";

        try (Connection connection = criarConexao();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, usuario.getNome().trim());
            statement.setString(2, usuario.getEmail().trim().toLowerCase());
            statement.setString(3, usuario.getSenha());
            statement.setString(4, usuario.getTipo().name());

            int linhasAfetadas = statement.executeUpdate();

            if (linhasAfetadas == 0) {
                throw new RuntimeException("Falha ao cadastrar usuário.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    return new Usuario(id, usuario.getNome().trim(), usuario.getEmail().trim().toLowerCase(), usuario.getSenha(), usuario.getTipo());
                }

                throw new RuntimeException("Falha ao recuperar ID do usuário cadastrado.");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Falha ao salvar usuário.", exception);
        }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        String sql = "SELECT id, nome, email, senha, tipo FROM usuarios WHERE lower(email) = lower(?) LIMIT 1";

        try (Connection connection = criarConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email.trim());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Long id = resultSet.getLong("id");
                    String nome = resultSet.getString("nome");
                    String emailSalvo = resultSet.getString("email");
                    String senha = resultSet.getString("senha");
                    TipoUsuario tipo = TipoUsuario.valueOf(resultSet.getString("tipo"));

                    return Optional.of(new Usuario(id, nome, emailSalvo, senha, tipo));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Falha ao consultar usuário por e-mail.", exception);
        }

        return Optional.empty();
    }

    private Connection criarConexao() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    private void criarTabelaSeNecessario() {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "nome TEXT NOT NULL, "
            + "email TEXT NOT NULL UNIQUE, "
            + "senha TEXT NOT NULL, "
            + "tipo TEXT NOT NULL" + ")";

        try (Connection connection = criarConexao();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new RuntimeException("Falha ao criar tabela de usuários.", exception);
        }
    }
}
