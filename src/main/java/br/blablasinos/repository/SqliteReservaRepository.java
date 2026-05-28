package br.blablasinos.repository;

import br.blablasinos.model.Reserva;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class SqliteReservaRepository implements ReservaRepository {

    private final String databaseUrl;

    public SqliteReservaRepository() {
        this.databaseUrl = "jdbc:sqlite:caronas.db";
        // ESTA É A CORREÇÃO: Garante que a tabela 'reservas' seja criada ao iniciar.
        criarTabelaSeNecessario();
    }

    private void criarTabelaSeNecessario() {
        String sql = """
            CREATE TABLE IF NOT EXISTS reservas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                carona_id INTEGER NOT NULL,
                passageiro_id INTEGER NOT NULL,
                status TEXT NOT NULL,
                FOREIGN KEY (carona_id) REFERENCES caronas (id),
                FOREIGN KEY (passageiro_id) REFERENCES usuarios (id)
            );
            """;
        try (Connection conn = criarConexao(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao criar tabela de reservas.", e);
        }
    }

    private Connection criarConexao() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    @Override
    public Reserva salvar(Reserva reserva) {
        String sql = "INSERT INTO reservas(carona_id, passageiro_id, status) VALUES(?, ?, ?)";
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, reserva.getCaronaId());
            pstmt.setLong(2, reserva.getPassageiroId());
            pstmt.setString(3, reserva.getStatus());
            
            int linhasAfetadas = pstmt.executeUpdate();
            if (linhasAfetadas == 0) {
                throw new SQLException("Falha ao criar a reserva, nenhuma linha afetada.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reserva.setId(generatedKeys.getLong(1));
                    return reserva;
                } else {
                    throw new SQLException("Falha ao obter o ID da reserva criada.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao salvar a reserva no banco de dados.", e);
        }
    }

    @Override
    public Optional<Reserva> buscarPorId(long id) {
        String sql = "SELECT * FROM reservas WHERE id = ?";
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Reserva reserva = new Reserva(
                    rs.getLong("id"),
                    rs.getLong("carona_id"),
                    rs.getLong("passageiro_id"),
                    rs.getString("status")
                );
                return Optional.of(reserva);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar reserva por ID.", e);
        }
        return Optional.empty();
    }

    @Override
    public void update(Reserva reserva) {
        String sql = "UPDATE reservas SET status = ? WHERE id = ?";
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reserva.getStatus());
            pstmt.setLong(2, reserva.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao atualizar status da reserva.", e);
        }
    }

    @Override
    public List<Reserva> listarPendentesPorMotorista(long motoristaId) {
        String sql = """
            SELECT r.*
            FROM reservas r
            JOIN caronas c ON c.id = r.carona_id
            WHERE c.motorista_id = ?
              AND r.status = 'PENDENTE'
            """;
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, motoristaId);
            ResultSet rs = pstmt.executeQuery();

            java.util.List<Reserva> reservas = new java.util.ArrayList<>();
            while (rs.next()) {
                Reserva reserva = new Reserva(
                    rs.getLong("id"),
                    rs.getLong("carona_id"),
                    rs.getLong("passageiro_id"),
                    rs.getString("status")
                );
                reservas.add(reserva);
            }
            return reservas;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar reservas pendentes.", e);
        }
    }

    @Override
    public void deletar(long id) {
        String sql = "DELETE FROM reservas WHERE id = ?";
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao deletar a reserva.", e);
        }
    }

    @Override
    public List<Reserva> listarPorPassageiro(long passageiroId) {
        String sql = "SELECT * FROM reservas WHERE passageiro_id = ? ORDER BY id DESC";
        try (Connection conn = criarConexao();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, passageiroId);
            ResultSet rs = pstmt.executeQuery();

            java.util.List<Reserva> reservas = new java.util.ArrayList<>();
            while (rs.next()) {
                Reserva reserva = new Reserva(
                    rs.getLong("id"),
                    rs.getLong("carona_id"),
                    rs.getLong("passageiro_id"),
                    rs.getString("status")
                );
                reservas.add(reserva);
            }
            return reservas;
        } catch (SQLException e) {
            throw new RuntimeException("Falha ao listar reservas do passageiro.", e);
        }
    }
}