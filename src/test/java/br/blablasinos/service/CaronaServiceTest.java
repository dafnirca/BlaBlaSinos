package br.blablasinos.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.blablasinos.model.Carona;
import br.blablasinos.model.TipoUsuario;
import br.blablasinos.model.Usuario;
import br.blablasinos.repository.CaronaRepository;
import br.blablasinos.repository.UsuarioRepository;
import br.blablasinos.service.CaronaService.CaronaException;

public class CaronaServiceTest {

    private CaronaService caronaService;
    private FakeCaronaRepository caronaRepository;
    private InMemoryUsuarioRepository usuarioRepository;

    private static final Long MOTORISTA_ID = 1L;
    private final LocalDateTime horarioValido = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0);

    @BeforeEach
    public void setUp() {
        caronaRepository = new FakeCaronaRepository();
        usuarioRepository = new InMemoryUsuarioRepository();
        caronaService = new CaronaService(caronaRepository, usuarioRepository);

        Usuario motorista = new Usuario(MOTORISTA_ID, "João Pedro", "joao@edu.unisinos.br", "senha123", TipoUsuario.MOTORISTA);
        motorista.setCnh("123456789");
        motorista.setModeloVeiculo("Fusca");
        motorista.setCorVeiculo("Azul");
        motorista.setPlacaVeiculo("ABC1234");

        usuarioRepository.mapearUsuario(MOTORISTA_ID.toString(), motorista);
        usuarioRepository.salvar(motorista);
    }


    @Test
    public void deveCadastrarCaronaComDadosValidos() throws CaronaException, SQLException {
        Carona carona = caronaService.cadastrarCarona(
            MOTORISTA_ID,
            "Rua das Flores, 100 — São Leopoldo", 
            "Unisinos Porto Alegre",              
            horarioValido,
            3
        );

        assertNotNull(carona);
        assertNotNull(carona.getId());
        assertEquals(MOTORISTA_ID, carona.getMotoristaId());
        assertEquals(3, carona.getVagasTotais());
        assertEquals(3, carona.getVagasDisponiveis());
    }

    @Test
    public void deveReprovarCaronaSemCampusUnisinos_RN02_1() {
        assertNotNull(assertThrows(CaronaException.class, () -> 
            caronaService.cadastrarCarona(MOTORISTA_ID, "Rua A, Canoas", "Rua B, Novo Hamburgo", horarioValido, 2)
        ));
    }

    @Test
    public void deveReprovarCaronaComVagasInvalidas_RN02_2() {
        assertNotNull(assertThrows(CaronaException.class, () -> 
            caronaService.cadastrarCarona(MOTORISTA_ID, "Unisinos", "Casa", horarioValido, 0)
        ));

        assertNotNull(assertThrows(CaronaException.class, () -> 
            caronaService.cadastrarCarona(MOTORISTA_ID, "Unisinos", "Casa", horarioValido, 5)
        ));
    }

    @Test
    public void deveReprovarCaronaNoPassado_RN02_3() {
        LocalDateTime ontem = LocalDateTime.now().minusDays(1);
        assertNotNull(assertThrows(CaronaException.class, () -> 
            caronaService.cadastrarCarona(MOTORISTA_ID, "Unisinos", "Casa", ontem, 2)
        ));
    }

    @Test
    public void deveReprovarCaronaNoMesmoHorario_RN02_4() throws CaronaException, SQLException {
        caronaService.cadastrarCarona(MOTORISTA_ID, "Unisinos", "Casa", horarioValido, 2);

        assertNotNull(assertThrows(CaronaException.class, () -> 
            caronaService.cadastrarCarona(MOTORISTA_ID, "Unisinos", "Outro Lugar", horarioValido, 2)
        ));
    }

    @Test
    public void deveReprovarCadastroSeUsuarioForPassageiro_RN02_5() {
        Long idPassageiro = 2L;
        Usuario passageiro = new Usuario(idPassageiro, "Laura", "laura@edu.unisinos.br", "senha123", TipoUsuario.PASSAGEIRO);
        usuarioRepository.mapearUsuario(idPassageiro.toString(), passageiro);

        assertNotNull(assertThrows(CaronaException.class, () -> 
            caronaService.cadastrarCarona(idPassageiro, "Unisinos", "Casa", horarioValido, 2)
        ));
    }

    @Test
    public void deveReprovarMotoristaComPerfilIncompleto_RN02_5() {
        Long idIncompleto = 3L;
        Usuario incompleto = new Usuario(idIncompleto, "Bruno", "bruno@edu.unisinos.br", "senha123", TipoUsuario.MOTORISTA);
        usuarioRepository.mapearUsuario(idIncompleto.toString(), incompleto);

        assertNotNull(assertThrows(CaronaException.class, () -> 
            caronaService.cadastrarCarona(idIncompleto, "Unisinos", "Casa", horarioValido, 2)
        ));
    }


    @Test
    public void deveEditarCaronaComSucesso() throws CaronaException, SQLException {
        Carona carona = caronaService.cadastrarCarona(MOTORISTA_ID, "Unisinos SL", "Casa", horarioValido, 2);
        LocalDateTime novoHorario = horarioValido.plusHours(2);

        Carona editada = caronaService.editarCarona(
            MOTORISTA_ID, carona.getId(), "Unisinos POA", "Novo Destino", novoHorario, 4
        );

        assertEquals("Unisinos POA", editada.getOrigem());
        assertEquals("Novo Destino", editada.getDestino());
        assertEquals(novoHorario, editada.getDataHora());
        assertEquals(4, editada.getVagasTotais());
    }

    @Test
    public void deveReprovarEdicaoSeReduzirVagasAbaixoDasReservadas() throws CaronaException, SQLException {
        Carona carona = caronaService.cadastrarCarona(MOTORISTA_ID, "Unisinos", "Casa", horarioValido, 4);
        
        carona.setVagasDisponiveis(2); 
        caronaRepository.atualizarEdicao(carona);

        CaronaException excecao = assertThrows(CaronaException.class, () -> 
            caronaService.editarCarona(MOTORISTA_ID, carona.getId(), null, null, null, 1)
        );
        assertNotNull(excecao);
    }


    @Test
    public void deveCancelarCaronaComSucesso() throws CaronaException, SQLException {
        Carona carona = caronaService.cadastrarCarona(MOTORISTA_ID, "Unisinos", "Casa", horarioValido, 2);
        
        assertDoesNotThrow(() -> caronaService.cancelarCarona(MOTORISTA_ID, carona.getId()));
        assertTrue(caronaRepository.buscarPorId(carona.getId()).isEmpty());
    }

    @Test
    public void deveReprovarCancelamentoEEdicaoDeCaronaNoPassado_RN02_6() throws SQLException {
        LocalDateTime passado = LocalDateTime.now().minusHours(2);
        Carona caronaAntiga = new Carona(99L, MOTORISTA_ID, "Unisinos", "Casa", passado, 2, 2);
        caronaRepository.salvar(caronaAntiga);

        assertNotNull(assertThrows(CaronaException.class, () -> 
            caronaService.cancelarCarona(MOTORISTA_ID, caronaAntiga.getId())
        ));

        assertNotNull(assertThrows(CaronaException.class, () -> 
            caronaService.editarCarona(MOTORISTA_ID, caronaAntiga.getId(), "Unisinos", "Novo", null, 3)
        ));
    }


    @Test
    public void deveBuscarCaronasDisponiveisComSucesso() throws SQLException, CaronaException {
        Carona carona1 = new Carona(10L, MOTORISTA_ID, "Casa", "Unisinos POA", horarioValido, 3, 3);
        Carona carona2 = new Carona(11L, MOTORISTA_ID, "Unisinos SL", "Gramado", horarioValido, 2, 2);
        caronaRepository.salvar(carona1);
        caronaRepository.salvar(carona2);

        List<Carona> resultado = caronaService.buscarCaronasDisponiveis("Unisinos POA", "2026-05-20");

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty(), "A lista de caronas disponíveis não deveria estar vazia.");
        assertEquals(2, resultado.size());
    }

    @Test
    public void deveRetornarListaVaziaSeNaoHouverCaronas() throws SQLException {
        List<Carona> resultado = caronaService.buscarCaronasDisponiveis("Unisinos SL", "2026-05-20");

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }


    private static class FakeCaronaRepository extends CaronaRepository {
        private final Map<Long, Carona> bancoEmMemoria = new HashMap<>();
        private long idContador = 1;

        public FakeCaronaRepository() {
            super("jdbc:sqlite::memory:");
        }

        @Override
        public void criarTabela() throws SQLException { }

        @Override
        public void salvar(Carona carona) throws SQLException {
            if (carona.getId() == null) {
                carona.setId(idContador++);
            }
            bancoEmMemoria.put(carona.getId(), carona);
        }

        @Override
        public Optional<Carona> buscarPorId(Long id) throws SQLException {
            return Optional.ofNullable(bancoEmMemoria.get(id));
        }

        @Override
        public List<Carona> listarAtivas(String destino, String data) throws SQLException {
            return new ArrayList<>(bancoEmMemoria.values());
        }

        @Override
        public List<Carona> listarPorMotorista(Long motoristaId) throws SQLException {
            List<Carona> filtradas = new ArrayList<>();
            for (Carona c : bancoEmMemoria.values()) {
                if (c.getMotoristaId().equals(motoristaId)) filtradas.add(c);
            }
            return filtradas;
        }

        @Override
        public boolean existeConflitoHorario(Long motoristaId, LocalDateTime horario) throws SQLException {
            return bancoEmMemoria.values().stream()
                    .anyMatch(c -> c.getMotoristaId().equals(motoristaId) && c.getDataHora().equals(horario));
        }

        @Override
        public boolean existeConflitoHorarioExcluindo(Long motoristaId, LocalDateTime horario, Long caronaId) throws SQLException {
            return bancoEmMemoria.values().stream()
                    .anyMatch(c -> c.getMotoristaId().equals(motoristaId) 
                                && !c.getId().equals(caronaId) 
                                && c.getDataHora().equals(horario));
        }

        @Override
        public void atualizarEdicao(Carona carona) throws SQLException {
            bancoEmMemoria.put(carona.getId(), carona);
        }

        @Override
        public void deletar(Long caronaId) throws SQLException {
            bancoEmMemoria.remove(caronaId);
        }

        @Override
        public void atualizar(Carona carona) throws SQLException {
            bancoEmMemoria.put(carona.getId(), carona);
        }
    }

    private static class InMemoryUsuarioRepository implements UsuarioRepository {

        private final Map<String, Usuario> usuariosPorEmail = new HashMap<>();
        private long nextId = 1;

        @Override
        public boolean existsByEmail(String email) {
            if (email == null) return false;
            return usuariosPorEmail.containsKey(email.trim().toLowerCase());
        }

        @Override
        public Usuario salvar(Usuario usuario) {
            if (usuario.getId() == null) {
                usuario.setId(nextId++);
            }
            String email = usuario.getEmail().trim().toLowerCase();
            
            usuariosPorEmail.put(email, usuario);
            
            usuariosPorEmail.put(usuario.getId().toString(), usuario);
            
            return usuario;
        }

        @Override
        public Optional<Usuario> buscarPorEmail(String email) {
            if (email == null) return Optional.empty();
            return Optional.ofNullable(usuariosPorEmail.get(email.trim().toLowerCase()));
        }

        @Override
        public void atualizarStatusDeBloqueio(String email, int tentativasFalhas, Long bloqueadoAte) {
            Optional<Usuario> usuarioSalvo = buscarPorEmail(email);
            if (usuarioSalvo.isPresent()) {
                Usuario usuario = usuarioSalvo.get();
                usuario.setTentativasFalhas(tentativasFalhas);
                usuario.setBloqueadoAte(bloqueadoAte);
            }
        }

        @Override
        public void update(Usuario usuario) {}

        public void mapearUsuario(String chave, Usuario usuario) {
            if (chave == null) return;
            usuariosPorEmail.put(chave.trim().toLowerCase(), usuario);
        }
    }
}