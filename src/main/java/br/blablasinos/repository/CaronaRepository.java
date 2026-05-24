package br.blablasinos.repository;

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

import br.blablasinos.model.Carona;

public class CaronaRepository {

    private final String url; 

    public CaronaRepository(String jdbcUrl) {
        this.url = jdbcUrl;
        try {
            // ESTA É A CORREÇÃO: Garante que a tabela seja criada ao iniciar.
            criarTabela();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao inicializar e criar tabela de caronas.", e);
        }
    }


    public void criarTabela() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS caronas (
                id                INTEGER PRIMARY KEY AUTOINCREMENT,
                motorista_id      INTEGER NOT NULL,
                origem            TEXT    NOT NULL,
                destino           TEXT    NOT NULL,
                horario_saida     TEXT    NOT NULL,
                vagas_total       INTEGER NOT NULL,
                vagas_disponiveis INTEGER NOT NULL,
                observacoes       TEXT    DEFAULT '',
                status            TEXT    NOT NULL DEFAULT 'ATIVA',
                criado_em         TEXT    NOT NULL,
                FOREIGN KEY (motorista_id) REFERENCES usuarios(id)
            );
            """;
        try (Connection conn = DriverManager.getConnection(url);
             Statement  stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }


    public void salvar(Carona carona) throws SQLException {
        String sql = """
            INSERT INTO caronas
                (motorista_id, origem, destino, horario_saida,
                 vagas_total, vagas_disponiveis, observacoes, status, criado_em)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong  (1, carona.getMotoristaId());
            ps.setString(2, carona.getOrigem());
            ps.setString(3, carona.getDestino());
            ps.setString(4, carona.getDataHora().toString());
            ps.setInt   (5, carona.getVagasTotais());
            ps.setInt   (6, carona.getVagasDisponiveis());
            ps.setString(7, ""); // observacoes padrão
            ps.setString(8, "ATIVA"); // status padrão
            ps.setString(9, LocalDateTime.now().toString()); // criado_em

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    carona.setId(rs.getLong(1));
                }
            }
        }
    }


    public Optional<Carona> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM caronas WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public List<Carona> listarAtivas(String destino, String data) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT * FROM caronas
            WHERE status = 'ATIVA'
              AND vagas_disponiveis > 0
              AND horario_saida > ?
            """);

        List<Object> params = new ArrayList<>();
        params.add(LocalDateTime.now().toString()); 

        if (destino != null && !destino.isBlank()) {
            sql.append(" AND LOWER(destino) LIKE LOWER(?) ");
            params.add("%" + destino.trim() + "%");
        }
        if (data != null && !data.isBlank()) {
            sql.append(" AND DATE(horario_saida) = ? ");
            params.add(data.trim());
        }

        sql.append(" ORDER BY horario_saida ASC");

        List<Carona> resultado = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(mapear(rs));
            }
        }
        return resultado;
    }

    public List<Carona> listarPorMotorista(Long motoristaId) throws SQLException {
        String sql = """
            SELECT * FROM caronas
            WHERE motorista_id = ?
            ORDER BY horario_saida DESC
            """;

        List<Carona> resultado = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, motoristaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) resultado.add(mapear(rs));
            }
        }
        return resultado;
    }

    public boolean existeConflitoHorario(Long motoristaId, LocalDateTime horario) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM caronas
            WHERE motorista_id = ?
              AND status = 'ATIVA'
              AND ABS(CAST((julianday(horario_saida) - julianday(?)) * 24 * 60 AS INTEGER)) < 60
            """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong  (1, motoristaId);
            ps.setString(2, horario.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public boolean existeConflitoHorarioExcluindo(Long motoristaId, LocalDateTime horario, Long caronaId) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM caronas
            WHERE motorista_id = ?
              AND id <> ?
              AND status = 'ATIVA'
              AND ABS(CAST((julianday(horario_saida) - julianday(?)) * 24 * 60 AS INTEGER)) < 60
            """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong  (1, motoristaId);
            ps.setLong  (2, caronaId);
            ps.setString(3, horario.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void atualizarEdicao(Carona carona) throws SQLException {
        String sql = """
            UPDATE caronas
            SET origem = ?, destino = ?, horario_saida = ?, vagas_total = ?, vagas_disponiveis = ?
            WHERE id = ?
            """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, carona.getOrigem());
            ps.setString(2, carona.getDestino());
            ps.setString(3, carona.getDataHora().toString());
            ps.setInt   (4, carona.getVagasTotais());
            ps.setInt   (5, carona.getVagasDisponiveis());
            ps.setLong  (6, carona.getId());

            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                throw new SQLException("Carona com id=" + carona.getId() + " não encontrada para atualização.");
            }
        }
    }

    public void deletar(Long caronaId) throws SQLException {
        String sql = "DELETE FROM caronas WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, caronaId);
            ps.executeUpdate();
        }
    }

    public void atualizar(Carona carona) throws SQLException {
        String sql = """
            UPDATE caronas
            SET status = ?, vagas_disponiveis = ?
            WHERE id = ?
            """;

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "ATIVA"); 
            ps.setInt   (2, carona.getVagasDisponiveis());
            ps.setLong  (3, carona.getId());

            int lines = ps.executeUpdate();
            if (lines == 0) {
                throw new SQLException("Carona com id=" + carona.getId() + " não encontrada.");
            }
        }
    }


    private Carona mapear(ResultSet rs) throws SQLException {
        return new Carona(
            rs.getLong  ("id"),
            rs.getLong  ("motorista_id"),
            rs.getString("origem"),
            rs.getString("destino"),
            LocalDateTime.parse(rs.getString("horario_saida")),
            rs.getInt   ("vagas_total"),
            rs.getInt   ("vagas_disponiveis")
        );
    }
}