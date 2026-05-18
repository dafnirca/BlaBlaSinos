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

        String sql = "SELECT id, nome, email, senha, tipo, tentativas_falhas, bloqueado_ate FROM usuarios WHERE lower(email) = lower(?) LIMIT 1";

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
                    int tentativasFalhas = resultSet.getInt("tentativas_falhas");
                    long bloqueadoAte = resultSet.getLong("bloqueado_ate");
                    Long bloqueadoAteValue = resultSet.wasNull() ? null : bloqueadoAte;

                    return Optional.of(new Usuario(id, nome, emailSalvo, senha, tipo, tentativasFalhas, bloqueadoAteValue));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Falha ao consultar usuário por e-mail.", exception);
        }

        return Optional.empty();
    }

    @Override
    public void atualizarStatusDeBloqueio(String email, int tentativasFalhas, Long bloqueadoAte) {
        String sql = "UPDATE usuarios SET tentativas_falhas = ?, bloqueado_ate = ? WHERE lower(email) = lower(?)";

        try (Connection connection = criarConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, tentativasFalhas);

            if (bloqueadoAte == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
            } else {
                statement.setLong(2, bloqueadoAte);
            }

            statement.setString(3, email.trim());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Falha ao atualizar status de bloqueio.", exception);
        }
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
            + "tipo TEXT NOT NULL, "
            + "tentativas_falhas INTEGER NOT NULL DEFAULT 0, "
            + "bloqueado_ate INTEGER" + ")";

        try (Connection connection = criarConexao();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
            criarColunaSeNaoExistir(connection, "usuarios", "tentativas_falhas", "INTEGER NOT NULL DEFAULT 0");
            criarColunaSeNaoExistir(connection, "usuarios", "bloqueado_ate", "INTEGER");
            
            // ADICIONE ESTAS LINHAS
            criarColunaSeNaoExistir(connection, "usuarios", "cnh", "TEXT");
            criarColunaSeNaoExistir(connection, "usuarios", "modelo_veiculo", "TEXT");
            criarColunaSeNaoExistir(connection, "usuarios", "cor_veiculo", "TEXT");
            criarColunaSeNaoExistir(connection, "usuarios", "placa_veiculo", "TEXT");

        } catch (SQLException exception) {
            throw new RuntimeException("Falha ao criar tabela de usuários.", exception);
        }
    }

    // NOVO MÉTODO
    @Override
    public void update(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, cnh = ?, modelo_veiculo = ?, cor_veiculo = ?, placa_veiculo = ? WHERE id = ?";

        try (Connection connection = criarConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, usuario.getNome());
            statement.setString(2, usuario.getCnh());
            statement.setString(3, usuario.getModeloVeiculo());
            statement.setString(4, usuario.getCorVeiculo());
            statement.setString(5, usuario.getPlacaVeiculo());
            statement.setLong(6, usuario.getId());
            
            int linhasAfetadas = statement.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new RuntimeException("Falha ao atualizar perfil, usuário não encontrado com id: " + usuario.getId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar perfil do usuário.", e);
        }
    }


    private void criarColunaSeNaoExistir(Connection connection, String tabela, String coluna, String definicao) throws SQLException {
        String pragma = "PRAGMA table_info(" + tabela + ")";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(pragma)) {
            boolean existe = false;

            while (resultSet.next()) {
                if (coluna.equalsIgnoreCase(resultSet.getString("name"))) {
                    existe = true;
                    break;
                }
            }

            if (!existe) {
                statement.execute("ALTER TABLE " + tabela + " ADD COLUMN " + coluna + " " + definicao);
            }
        }
    }
}
