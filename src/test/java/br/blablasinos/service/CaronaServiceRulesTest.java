package br.blablasinos.service;

import br.blablasinos.model.Carona;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CaronaServiceRulesTest {

    private CaronaService service;
    private FakeCaronaRepository fakeCaronaRepo;
    private InMemoryUsuarioRepository inMemoryUsuarioRepo;
    private FakeReservaRepository fakeReservaRepo;

    @BeforeEach
    void setUp() {
        fakeCaronaRepo = new FakeCaronaRepository();
        inMemoryUsuarioRepo = new InMemoryUsuarioRepository();
        fakeReservaRepo = new FakeReservaRepository();
        service = new CaronaService(fakeCaronaRepo, inMemoryUsuarioRepo, fakeReservaRepo);
    }

    @Test
    void deveReprovarVagasForaDoIntervalo() throws SQLException {
        Usuario motorista = new Usuario(1L, "M", "m@teste.com", "senha123", TipoUsuario.MOTORISTA);
        motorista.setCnh("123");
        motorista.setModeloVeiculo("X");
        motorista.setCorVeiculo("C");
        motorista.setPlacaVeiculo("P");
        inMemoryUsuarioRepo.salvar(motorista);

        LocalDateTime saida = LocalDateTime.now().plusHours(2);

        CaronaService.CaronaException e1 = assertThrows(CaronaService.CaronaException.class, () ->
                service.cadastrarCarona(motorista.getId(), "Unisinos", "Porto Alegre", saida, 0)
        );
        assertTrue(e1.getMessage().contains("O número de vagas deve ser entre"));

        CaronaService.CaronaException e2 = assertThrows(CaronaService.CaronaException.class, () ->
                service.cadastrarCarona(motorista.getId(), "Unisinos", "Porto Alegre", saida, 5)
        );
        assertTrue(e2.getMessage().contains("O número de vagas deve ser entre"));
    }

    @Test
    void deveReprovarCampusInvalido() throws SQLException {
        Usuario motorista = new Usuario(2L, "M2", "m2@teste.com", "senha123", TipoUsuario.MOTORISTA);
        motorista.setCnh("123");
        motorista.setModeloVeiculo("X");
        motorista.setCorVeiculo("C");
        motorista.setPlacaVeiculo("P");
        inMemoryUsuarioRepo.salvar(motorista);

        LocalDateTime saida = LocalDateTime.now().plusHours(3);

        CaronaService.CaronaException e = assertThrows(CaronaService.CaronaException.class, () ->
                service.cadastrarCarona(motorista.getId(), "Centro", "Praia", saida, 2)
        );
        assertTrue(e.getMessage().contains("A origem ou o destino deve ser um campus Unisinos"));
    }

    @Test
    void deveReprovarDataHoraNaoFutura() throws SQLException {
        Usuario motorista = new Usuario(3L, "M3", "m3@teste.com", "senha123", TipoUsuario.MOTORISTA);
        motorista.setCnh("123");
        motorista.setModeloVeiculo("X");
        motorista.setCorVeiculo("C");
        motorista.setPlacaVeiculo("P");
        inMemoryUsuarioRepo.salvar(motorista);

        LocalDateTime passada = LocalDateTime.now().minusHours(1);

        CaronaService.CaronaException e = assertThrows(CaronaService.CaronaException.class, () ->
                service.cadastrarCarona(motorista.getId(), "Unisinos", "Centro", passada, 2)
        );
        assertTrue(e.getMessage().contains("O horário de saída deve ser posterior"));
    }

    @Test
    void deveReprovarPerfilNaoMotoristaOuIncompleto() throws SQLException {
        Usuario passageiro = new Usuario(4L, "P", "p@teste.com", "senha123", TipoUsuario.PASSAGEIRO);
        inMemoryUsuarioRepo.salvar(passageiro);

        LocalDateTime saida = LocalDateTime.now().plusHours(2);

        CaronaService.CaronaException e1 = assertThrows(CaronaService.CaronaException.class, () ->
                service.cadastrarCarona(passageiro.getId(), "Unisinos", "Centro", saida, 2)
        );
        assertTrue(e1.getMessage().contains("Apenas usuários com perfil Motorista"));

        Usuario motorista = new Usuario(5L, "M4", "m4@teste.com", "senha123", TipoUsuario.MOTORISTA);
        inMemoryUsuarioRepo.salvar(motorista);

        CaronaService.CaronaException e2 = assertThrows(CaronaService.CaronaException.class, () ->
                service.cadastrarCarona(motorista.getId(), "Unisinos", "Centro", saida, 2)
        );
        assertTrue(e2.getMessage().contains("perfil de motorista está incompleto"));
    }

    static class FakeReservaRepository implements ReservaRepository {
        @Override
        public br.blablasinos.model.Reserva salvar(br.blablasinos.model.Reserva r) {
            return r;
        }

        @Override
        public Optional<br.blablasinos.model.Reserva> buscarPorId(long id) {
            return Optional.empty();
        }

        @Override
        public void update(br.blablasinos.model.Reserva reserva) {
        }

        @Override
        public void deletar(long id) {
        }

        @Override
        public java.util.List<br.blablasinos.model.Reserva> listarPendentesPorMotorista(long motoristaId) {
            return java.util.List.of();
        }

        @Override
        public java.util.List<br.blablasinos.model.Reserva> listarPorPassageiro(long passageiroId) {
            return java.util.List.of();
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
        private long nextId = 1;

        public FakeCaronaRepository() {
            super("jdbc:sqlite::memory:");
        }

        @Override
        public void criarTabela() {
        }

        @Override
        public void salvar(Carona carona) {
            carona.setId(nextId++);
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
        public java.util.List<Carona> listarPorMotorista(Long motoristaId) {
            return java.util.List.copyOf(db.values());
        }

        @Override
        public java.util.List<Carona> listarAtivas(String destino, String data) {
            return java.util.List.of();
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

