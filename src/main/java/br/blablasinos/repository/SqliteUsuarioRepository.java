package br.blablasinos.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Optional;

import br.blablasinos.model.TipoUsuario;
import br.blablasinos.model.Usuario;

public class SqliteUsuarioRepository implements UsuarioRepository {

    private static final String DEFAULT_URL = "jdbc:sqlite:caronas.db";
    private final String databaseUrl;

    public SqliteUsuarioRepository() { this(DEFAULT_URL); }

    public SqliteUsuarioRepository(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        criarTabelaSeNecessario();
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) return false;
        String sql = "SELECT 1 FROM usuarios WHERE lower(email) = lower(?) LIMIT 1";
        try (Connection c = criarConexao(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, email.trim());
            try (ResultSet rs = s.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios(nome, email, senha, tipo) VALUES(?, ?, ?, ?)";
        try (Connection c = criarConexao(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, usuario.getNome().trim());
            s.setString(2, usuario.getEmail().trim().toLowerCase());
            s.setString(3, usuario.getSenha());
            s.setString(4, usuario.getTipo().name());
            if (s.executeUpdate() == 0) throw new RuntimeException("Falha ao cadastrar.");
            try (ResultSet keys = s.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setId(keys.getLong(1));
                    return usuario;
                }
                throw new RuntimeException("Falha ao recuperar ID.");
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE lower(email) = lower(?) LIMIT 1";
        try (Connection c = criarConexao(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, email.trim());
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) return Optional.of(mapearUsuario(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        String sql = "SELECT * FROM usuarios WHERE id = ? LIMIT 1";
        try (Connection c = criarConexao(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, id);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) return Optional.of(mapearUsuario(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public void update(Usuario usuario) {
        // A QUERY SQL AGORA INCLUI A COLUNA 'tipo'
        String sql = "UPDATE usuarios SET nome = ?, cnh = ?, modelo_veiculo = ?, cor_veiculo = ?, placa_veiculo = ?, tipo = ? WHERE id = ?";
        try (Connection c = criarConexao(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, usuario.getNome());
            s.setString(2, usuario.getCnh());
            s.setString(3, usuario.getModeloVeiculo());
            s.setString(4, usuario.getCorVeiculo());
            s.setString(5, usuario.getPlacaVeiculo());
            s.setString(6, usuario.getTipo().name()); // Adiciona o tipo do usuário
            s.setLong(7, usuario.getId());
            if (s.executeUpdate() == 0) throw new RuntimeException("Falha ao atualizar perfil, usuário não encontrado com id: " + usuario.getId());
        } catch (SQLException e) { throw new RuntimeException("Falha ao atualizar perfil do usuário.", e); }
    }

    @Override
    public void atualizarStatusDeBloqueio(String email, int tentativasFalhas, Long bloqueadoAte) {
        String sql = "UPDATE usuarios SET tentativas_falhas = ?, bloqueado_ate = ? WHERE lower(email) = lower(?)";
        try (Connection c = criarConexao(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, tentativasFalhas);
            if (bloqueadoAte == null) s.setNull(2, Types.BIGINT);
            else s.setLong(2, bloqueadoAte);
            s.setString(3, email.trim());
            s.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario(
            rs.getLong("id"), rs.getString("nome"), rs.getString("email"),
            rs.getString("senha"), TipoUsuario.valueOf(rs.getString("tipo")),
            rs.getInt("tentativas_falhas"), rs.getLong("bloqueado_ate")
        );
        if (rs.wasNull()) u.setBloqueadoAte(null);
        u.setCnh(rs.getString("cnh"));
        u.setModeloVeiculo(rs.getString("modelo_veiculo"));
        u.setCorVeiculo(rs.getString("cor_veiculo"));
        u.setPlacaVeiculo(rs.getString("placa_veiculo"));
        return u;
    }

    private Connection criarConexao() throws SQLException { return DriverManager.getConnection(databaseUrl); }
    
    // Cria a tabela com todas as colunas necessárias se ainda não existir
    private void criarTabelaSeNecessario() {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "senha TEXT NOT NULL, " +
                "tipo TEXT NOT NULL, " +
                "cnh TEXT, " +
                "modelo_veiculo TEXT, " +
                "cor_veiculo TEXT, " +
                "placa_veiculo TEXT, " +
                "tentativas_falhas INTEGER DEFAULT 0, " +
                "bloqueado_ate BIGINT" +
                ");";

        try (Connection c = criarConexao(); Statement s = c.createStatement()) {
            s.execute(sql);

            // Garante colunas compatíveis em DBs antigos
            criarColunaSeNaoExistir(c, "usuarios", "cnh", "TEXT");
            criarColunaSeNaoExistir(c, "usuarios", "modelo_veiculo", "TEXT");
            criarColunaSeNaoExistir(c, "usuarios", "cor_veiculo", "TEXT");
            criarColunaSeNaoExistir(c, "usuarios", "placa_veiculo", "TEXT");
            criarColunaSeNaoExistir(c, "usuarios", "tentativas_falhas", "INTEGER DEFAULT 0");
            criarColunaSeNaoExistir(c, "usuarios", "bloqueado_ate", "BIGINT");
            criarColunaSeNaoExistir(c, "usuarios", "tipo", "TEXT NOT NULL DEFAULT 'PASSAGEIRO'");
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar ou atualizar a tabela usuarios", e);
        }
    }

    private void criarColunaSeNaoExistir(Connection c, String t, String col, String def) throws SQLException {
        String pragma = "PRAGMA table_info(" + t + ")";
        try (Statement s = c.createStatement(); ResultSet rs = s.executeQuery(pragma)) {
            boolean encontrado = false;
            while (rs.next()) {
                String nome = rs.getString("name");
                if (col.equalsIgnoreCase(nome)) { encontrado = true; break; }
            }
            if (!encontrado) {
                try (Statement s2 = c.createStatement()) {
                    s2.execute("ALTER TABLE " + t + " ADD COLUMN " + col + " " + def);
                }
            }
        }
    }
}