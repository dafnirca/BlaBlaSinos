package br.blablasinos.repository;

import br.blablasinos.model.TipoUsuario;
import br.blablasinos.model.Usuario;
import java.sql.*;
import java.util.Optional;

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
    
    // O código de criarTabelaSeNecessario e criarColunaSeNaoExistir permanece o mesmo
    private void criarTabelaSeNecessario() { /* ... seu código existente ... */ }
    private void criarColunaSeNaoExistir(Connection c, String t, String col, String def) throws SQLException { /* ... seu código existente ... */ }
}