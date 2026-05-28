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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CaronaServiceTest {

    private CaronaService service;
    private FakeCaronaRepository fakeCaronaRepo;
    private InMemoryUsuarioRepository inMemoryUsuarioRepo;
    private FakeReservaRepository fakeReservaRepo;
    private Usuario motorista;

    @BeforeEach
    void setUp() {
        fakeCaronaRepo = new FakeCaronaRepository();
        inMemoryUsuarioRepo = new InMemoryUsuarioRepository();
        fakeReservaRepo = new FakeReservaRepository();
        service = new CaronaService(fakeCaronaRepo, inMemoryUsuarioRepo, fakeReservaRepo);

        motorista = new Usuario(1L, "Motorista Teste", "motorista@teste.com", "senha123", TipoUsuario.MOTORISTA);
        motorista.setCnh("123456789");
        motorista.setModeloVeiculo("Carro de Teste");
        motorista.setCorVeiculo("Preto");
        motorista.setPlacaVeiculo("TST-0001");
        inMemoryUsuarioRepo.salvar(motorista);
    }

    @Test
    void deveCadastrarCaronaComDadosValidos() throws CaronaService.CaronaException, SQLException {
        LocalDateTime saida = LocalDateTime.now().plusHours(2);
        Carona carona = service.cadastrarCarona(motorista.getId(), "Unisinos São Leopoldo", "Centro POA", saida, 3);
        
        assertNotNull(carona);
        assertNotNull(carona.getId());
        assertEquals(motorista.getId(), carona.getMotoristaId());
    }

    @Test
    void deveReprovarCaronaNoMesmoHorario_RN02_4() throws CaronaService.CaronaException, SQLException {
        LocalDateTime saida = LocalDateTime.now().plusHours(3);
        // 1. Cadastra a primeira carona válida
        service.cadastrarCarona(motorista.getId(), "Campus São Leopoldo", "Centro", saida, 3);

        // 2. Tenta cadastrar a segunda carona com horário conflitante e LOCALIZAÇÃO VÁLIDA
        CaronaService.CaronaException e = assertThrows(CaronaService.CaronaException.class, () -> {
            // CORREÇÃO: Usamos "Porto Alegre" para passar na validação de campus
            service.cadastrarCarona(motorista.getId(), "Centro", "Porto Alegre", saida.plusMinutes(10), 2);
        });

        // 3. Verifica se a mensagem de erro é a de conflito de horário, como esperado
        String expectedMessage = "Você já possui uma carona ativa neste horário.";
        assertTrue(e.getMessage().startsWith(expectedMessage),
            "A mensagem de erro estava incorreta. Esperado que começasse com: '" + expectedMessage + "', mas foi: '" + e.getMessage() + "'"
        );
    }

    // ==================================================================
    // === CLASSES FALSAS PARA SIMULAR O BANCO DE DADOS NOS TESTES ===
    // ==================================================================

    static class FakeReservaRepository implements ReservaRepository {
        private final Map<Long, Reserva> db = new HashMap<>();
        private long nextId = 1;
        @Override public Reserva salvar(Reserva r) { r.setId(nextId++); db.put(r.getId(), r); return r; }
        @Override public Optional<Reserva> buscarPorId(long id) { return Optional.ofNullable(db.get(id)); }
        @Override public void update(Reserva reserva) { db.put(reserva.getId(), reserva); }
        @Override public void deletar(long id) { db.remove(id); }
        @Override public List<Reserva> listarPendentesPorMotorista(long motoristaId) {
            return db.values().stream()
                .filter(r -> "PENDENTE".equals(r.getStatus()))
                .collect(Collectors.toList());
        }
        @Override public List<Reserva> listarPorPassageiro(long passageiroId) {
            return db.values().stream()
                .filter(r -> r.getPassageiroId() == passageiroId)
                .collect(Collectors.toList());
        }
    }

    static class InMemoryUsuarioRepository implements UsuarioRepository {
        private final Map<Long, Usuario> db = new HashMap<>();
        private final Map<String, Usuario> dbEmail = new HashMap<>();
        @Override public Usuario salvar(Usuario u) { db.put(u.getId(), u); dbEmail.put(u.getEmail().toLowerCase(), u); return u; }
        @Override public Optional<Usuario> buscarPorId(Long id) { return Optional.ofNullable(db.get(id)); }
        @Override public Optional<Usuario> buscarPorEmail(String email) { return Optional.ofNullable(dbEmail.get(email.toLowerCase())); }
        @Override public boolean existsByEmail(String email) { return dbEmail.containsKey(email.toLowerCase()); }
        @Override public void atualizarStatusDeBloqueio(String e, int t, Long b) {}
        @Override public void update(Usuario u) { if (u != null && u.getId() != null) db.put(u.getId(), u); }
    }

    static class FakeCaronaRepository extends CaronaRepository {
        private final Map<Long, Carona> db = new HashMap<>();
        private long nextId = 1;

        public FakeCaronaRepository() { super("jdbc:sqlite::memory:"); }
        @Override public void criarTabela() { /* Não faz nada, usa o mapa */ }

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
        public List<Carona> listarPorMotorista(Long motoristaId) {
            return db.values().stream()
                .filter(c -> c.getMotoristaId().equals(motoristaId))
                .collect(Collectors.toList());
        }

        @Override
        public List<Carona> listarAtivas(String destino, String data) {
            return new ArrayList<>();
        }

        @Override
        public boolean existeConflitoHorario(Long motoristaId, LocalDateTime horario) {
            return db.values().stream().anyMatch(c ->
                c.getMotoristaId().equals(motoristaId) &&
                Math.abs(java.time.Duration.between(c.getDataHora(), horario).toMinutes()) < 60
            );
        }

        @Override
        public boolean existeConflitoHorarioExcluindo(Long motoristaId, LocalDateTime horario, Long caronaId) {
             return db.values().stream().anyMatch(c ->
                !c.getId().equals(caronaId) &&
                c.getMotoristaId().equals(motoristaId) &&
                Math.abs(java.time.Duration.between(c.getDataHora(), horario).toMinutes()) < 60
            );
        }
    }
}