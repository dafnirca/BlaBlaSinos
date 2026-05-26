package br.blablasinos.service;

import br.blablasinos.model.Carona;
import br.blablasinos.model.Reserva;
import br.blablasinos.model.TipoUsuario;
import br.blablasinos.model.Usuario;
import br.blablasinos.repository.CaronaRepository;
import br.blablasinos.repository.ReservaRepository;
import br.blablasinos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CaronaIntegrationTest {

    private CaronaService service;
    private FakeCaronaRepository caronaRepo;
    private InMemoryUsuarioRepository usuarioRepo;
    private InMemoryReservaRepository reservaRepo;

    @BeforeEach
    void setUp() {
        caronaRepo = new FakeCaronaRepository();
        usuarioRepo = new InMemoryUsuarioRepository();
        reservaRepo = new InMemoryReservaRepository();
        service = new CaronaService(caronaRepo, usuarioRepo, reservaRepo);
    }

    @Test
    void fluxoSolicitacaoAprovacaoCompleto() throws Exception {
        Usuario motorista = new Usuario(1L, "Motorista", "m@teste.com", "senha123", TipoUsuario.MOTORISTA);
        motorista.setCnh("111");
        motorista.setModeloVeiculo("Modelo");
        motorista.setCorVeiculo("Cor");
        motorista.setPlacaVeiculo("XXX-0001");
        usuarioRepo.salvar(motorista);

        LocalDateTime saida = LocalDateTime.now().plusHours(4);
        Carona carona = service.cadastrarCarona(motorista.getId(), "Unisinos São Leopoldo", "Centro POA", saida, 3);

        Usuario passageiro = new Usuario(2L, "Passageiro", "p@teste.com", "senha123", TipoUsuario.PASSAGEIRO);
        usuarioRepo.salvar(passageiro);

        Reserva reserva = service.solicitarVaga(passageiro.getId(), carona.getId());
        assertNotNull(reserva);
        assertEquals("PENDENTE", reserva.getStatus());

        List<Reserva> pendentes = service.listarSolicitacoesPendentes(motorista.getId());
        assertFalse(pendentes.isEmpty());
        Reserva pend = pendentes.get(0);

        Reserva decidida = service.decidirSolicitacao(motorista.getId(), pend.getId(), true);
        assertEquals("CONFIRMADA", decidida.getStatus());

        Carona caronaAtual = service.buscarCaronaPorId(carona.getId());
        assertEquals(carona.getVagasTotais() - 1, caronaAtual.getVagasDisponiveis());

        List<Reserva> reservasPassageiro = service.listarSolicitacoesDoPassageiro(passageiro.getId());
        assertTrue(reservasPassageiro.stream().anyMatch(r -> "CONFIRMADA".equals(r.getStatus())));
    }

    static class InMemoryReservaRepository implements ReservaRepository {
        private final Map<Long, Reserva> db = new HashMap<>();
        private long next = 1;

        @Override
        public Reserva salvar(Reserva r) {
            r.setId(next++);
            db.put(r.getId(), r);
            return r;
        }

        @Override
        public Optional<Reserva> buscarPorId(long id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public void update(Reserva reserva) {
            db.put(reserva.getId(), reserva);
        }

        @Override
        public void deletar(long id) {
            db.remove(id);
        }

        @Override
        public List<Reserva> listarPendentesPorMotorista(long motoristaId) {
            return db.values().stream().filter(r -> "PENDENTE".equals(r.getStatus())).toList();
        }

        @Override
        public List<Reserva> listarPorPassageiro(long passageiroId) {
            return db.values().stream().filter(r -> r.getPassageiroId() == passageiroId).toList();
        }
    }

    static class InMemoryUsuarioRepository implements UsuarioRepository {
        private final Map<Long, Usuario> db = new HashMap<>();

        @Override
        public boolean existsByEmail(String email) {
            return db.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
        }

        @Override
        public Usuario salvar(Usuario usuario) {
            db.put(usuario.getId(), usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorEmail(String email) {
            return db.values().stream().filter(u -> u.getEmail().equalsIgnoreCase(email)).findFirst();
        }

        @Override
        public Optional<Usuario> buscarPorId(Long id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public void atualizarStatusDeBloqueio(String email, int tentativasFalhas, Long bloqueadoAte) {
        }

        @Override
        public void update(Usuario usuario) {
            if (usuario != null && usuario.getId() != null) {
                db.put(usuario.getId(), usuario);
            }
        }
    }

    static class FakeCaronaRepository extends CaronaRepository {
        private final Map<Long, Carona> db = new HashMap<>();
        private long next = 1;

        public FakeCaronaRepository() {
            super("jdbc:sqlite::memory:");
        }

        @Override
        public void criarTabela() {
        }

        @Override
        public void salvar(Carona carona) {
            carona.setId(next++);
            db.put(carona.getId(), carona);
        }

        @Override
        public Optional<Carona> buscarPorId(Long id) {
            return Optional.ofNullable(db.get(id));
        }

        @Override
        public void atualizar(Carona carona) {
            db.put(carona.getId(), carona);
        }

        @Override
        public void atualizarEdicao(Carona carona) {
            db.put(carona.getId(), carona);
        }

        @Override
        public void deletar(Long caronaId) {
            db.remove(caronaId);
        }

        @Override
        public List<Carona> listarPorMotorista(Long motoristaId) {
            return db.values().stream().filter(c -> c.getMotoristaId().equals(motoristaId)).toList();
        }

        @Override
        public List<Carona> listarAtivas(String destino, String data) {
            return db.values().stream().toList();
        }

        @Override
        public boolean existeConflitoHorario(Long motoristaId, LocalDateTime horario) {
            return db.values().stream().anyMatch(c -> c.getMotoristaId().equals(motoristaId)
                    && Math.abs(java.time.Duration.between(c.getDataHora(), horario).toMinutes()) < 60);
        }

        @Override
        public boolean existeConflitoHorarioExcluindo(Long motoristaId, LocalDateTime horario, Long caronaId) {
            return db.values().stream().anyMatch(c -> !c.getId().equals(caronaId)
                    && c.getMotoristaId().equals(motoristaId)
                    && Math.abs(java.time.Duration.between(c.getDataHora(), horario).toMinutes()) < 60);
        }
    }
}

