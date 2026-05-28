package br.blablasinos.repository;

import br.blablasinos.model.Carona;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class SqliteCaronaRepository extends CaronaRepository {

    private final String databaseUrl;

    public SqliteCaronaRepository() {
        super("jdbc:sqlite:caronas.db");
        this.databaseUrl = "jdbc:sqlite:caronas.db";
    }

    private Connection criarConexao() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    public Optional<Carona> buscarPorId(Long id) {
        String sql = "SELECT * FROM caronas WHERE id = ?";
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Carona carona = new Carona();
                carona.setId(rs.getLong("id"));
                carona.setMotoristaId(rs.getLong("motorista_id"));
                carona.setOrigem(rs.getString("origem"));
                carona.setDestino(rs.getString("destino"));
                carona.setDataHora(LocalDateTime.parse(rs.getString("data_hora")));
                carona.setVagasDisponiveis(rs.getInt("vagas_disponiveis"));
                carona.setVagasTotais(rs.getInt("vagas_totais"));
                return Optional.of(carona);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar carona por ID.", e);
        }
        return Optional.empty();
    }

    public void update(Carona carona) {
        String sql = "UPDATE caronas SET vagas_disponiveis = ? WHERE id = ?";
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, carona.getVagasDisponiveis());
            pstmt.setLong(2, carona.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar carona.", e);
        }
    }
}