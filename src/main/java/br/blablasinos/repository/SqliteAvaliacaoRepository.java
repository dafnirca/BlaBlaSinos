package br.blablasinos.repository;

import br.blablasinos.model.Avaliacao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteAvaliacaoRepository implements AvaliacaoRepository {

    private final String databaseUrl;

    public SqliteAvaliacaoRepository() {
        this("jdbc:sqlite:caronas.db");
    }

    public SqliteAvaliacaoRepository(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        criarTabelaSeNecessario();
    }

    private void criarTabelaSeNecessario() {
        String sql = """
            CREATE TABLE IF NOT EXISTS avaliacoes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                carona_id INTEGER NOT NULL,
                avaliador_id INTEGER NOT NULL,
                avaliado_id INTEGER NOT NULL,
                nota INTEGER NOT NULL,
                comentario TEXT DEFAULT '',
                data_avaliacao TEXT NOT NULL,
                UNIQUE(carona_id, avaliador_id, avaliado_id),
                FOREIGN KEY (carona_id) REFERENCES caronas (id),
                FOREIGN KEY (avaliador_id) REFERENCES usuarios (id),
                FOREIGN KEY (avaliado_id) REFERENCES usuarios (id)
            );
            """;
        try (Connection conn = criarConexao(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela de avaliacoes.", e);
        }
    }

    private Connection criarConexao() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    @Override
    public Avaliacao salvar(Avaliacao avaliacao) {
        String sql = """
            INSERT INTO avaliacoes(carona_id, avaliador_id, avaliado_id, nota, comentario, data_avaliacao)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = criarConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, avaliacao.getIdCarona());
            ps.setLong(2, avaliacao.getIdAvaliador());
            ps.setLong(3, avaliacao.getIdAvaliado());
            ps.setInt(4, avaliacao.getNota());
            ps.setString(5, avaliacao.getComentario() == null ? "" : avaliacao.getComentario());
            ps.setString(6, avaliacao.getDataAvaliacao().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) avaliacao.setId(keys.getLong(1));
            }
            return avaliacao;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao salvar avaliacao.", e);
        }
    }

    @Override
    public Optional<Avaliacao> buscarPorCaronaEAvaliadorEAvaliado(long caronaId, long avaliadorId, long avaliadoId) {
        String sql = "SELECT * FROM avaliacoes WHERE carona_id = ? AND avaliador_id = ? AND avaliado_id = ?";
        try (Connection conn = criarConexao(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, caronaId);
            ps.setLong(2, avaliadorId);
            ps.setLong(3, avaliadoId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapear(rs));
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar avaliacao.", e);
        }
    }

    @Override
    public List<Avaliacao> listarRecebidasPorUsuario(long usuarioId) {
        String sql = "SELECT * FROM avaliacoes WHERE avaliado_id = ? ORDER BY data_avaliacao DESC";
        try (Connection conn = criarConexao(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, usuarioId);
            ResultSet rs = ps.executeQuery();
            List<Avaliacao> avaliacoes = new ArrayList<>();
            while (rs.next()) avaliacoes.add(mapear(rs));
            return avaliacoes;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar avaliacoes recebidas.", e);
        }
    }

    @Override
    public List<Avaliacao> listarPorCaronaEAvaliador(long caronaId, long avaliadorId) {
        String sql = "SELECT * FROM avaliacoes WHERE carona_id = ? AND avaliador_id = ?";
        try (Connection conn = criarConexao(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, caronaId);
            ps.setLong(2, avaliadorId);
            ResultSet rs = ps.executeQuery();
            List<Avaliacao> avaliacoes = new ArrayList<>();
            while (rs.next()) avaliacoes.add(mapear(rs));
            return avaliacoes;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar avaliacoes da carona.", e);
        }
    }

    private Avaliacao mapear(ResultSet rs) throws SQLException {
        return new Avaliacao(
            rs.getLong("id"),
            rs.getLong("carona_id"),
            rs.getLong("avaliador_id"),
            rs.getLong("avaliado_id"),
            rs.getInt("nota"),
            rs.getString("comentario"),
            LocalDateTime.parse(rs.getString("data_avaliacao"))
        );
    }
}
