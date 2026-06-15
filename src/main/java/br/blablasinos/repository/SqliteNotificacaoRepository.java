package br.blablasinos.repository;

import br.blablasinos.model.Notificacao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteNotificacaoRepository implements NotificacaoRepository {

    private final String databaseUrl;

    public SqliteNotificacaoRepository() {
        this.databaseUrl = "jdbc:sqlite:caronas.db";
        criarTabelaSeNecessario();
    }

    private Connection criarConexao() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    private void criarTabelaSeNecessario() {
        String sql = """
            CREATE TABLE IF NOT EXISTS notificacoes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                tipo TEXT NOT NULL,
                mensagem TEXT NOT NULL,
                referencia_id INTEGER,
                lida INTEGER NOT NULL DEFAULT 0,
                criada_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
            );
            """;
        try (Connection conn = criarConexao(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela de notificacoes.", e);
        }
    }

    @Override
    public Notificacao salvar(Notificacao notificacao) {
        String sql = "INSERT INTO notificacoes(usuario_id, tipo, mensagem, referencia_id, lida) VALUES(?, ?, ?, ?, ?)";
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, notificacao.getUsuarioId());
            pstmt.setString(2, notificacao.getTipo());
            pstmt.setString(3, notificacao.getMensagem());
            if (notificacao.getReferenciaId() == null) {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                pstmt.setLong(4, notificacao.getReferenciaId());
            }
            pstmt.setInt(5, notificacao.isLida() ? 1 : 0);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notificacao.setId(generatedKeys.getLong(1));
                }
            }
            return notificacao;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao salvar notificacao.", e);
        }
    }

    @Override
    public List<Notificacao> listarPorUsuario(long usuarioId) {
        String sql = "SELECT * FROM notificacoes WHERE usuario_id = ? ORDER BY id DESC";
        List<Notificacao> notificacoes = new ArrayList<>();
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notificacoes.add(mapRow(rs));
            }
            return notificacoes;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar notificacoes do usuario.", e);
        }
    }

    @Override
    public Optional<Notificacao> buscarPorId(long id) {
        String sql = "SELECT * FROM notificacoes WHERE id = ?";
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar notificacao por id.", e);
        }
    }

    @Override
    public void marcarComoLida(long id) {
        String sql = "UPDATE notificacoes SET lida = 1 WHERE id = ?";
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao marcar notificacao como lida.", e);
        }
    }

    private Notificacao mapRow(ResultSet rs) throws SQLException {
        return new Notificacao(
            rs.getLong("id"),
            rs.getLong("usuario_id"),
            rs.getString("tipo"),
            rs.getString("mensagem"),
            rs.getObject("referencia_id") == null ? null : rs.getLong("referencia_id"),
            rs.getInt("lida") == 1,
            rs.getString("criada_em")
        );
    }
}
