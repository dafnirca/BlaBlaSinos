package br.blablasinos.service;

import br.blablasinos.exception.UsuarioValidationException;
import br.blablasinos.model.TipoUsuario;
import br.blablasinos.model.Usuario;
import br.blablasinos.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuthServiceTest {

    private AuthService authService;
    private InMemoryUsuarioRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new InMemoryUsuarioRepository();
        authService = new AuthService(repository);
    }

    @Test
    public void deveAutenticarUsuarioValido() {
        Usuario usuario = criarUsuario("maria@edu.unisinos.br", "Senha1234");
        repository.salvar(usuario);

        Usuario resultado = authService.autenticar("maria@edu.unisinos.br", "Senha1234");

        assertNotNull(resultado);
        assertEquals("maria@edu.unisinos.br", resultado.getEmail());
    }

    @Test
    public void deveBloquearContaAposCincoTentativasFalhas() {
        Usuario usuario = criarUsuario("joao@edu.unisinos.br", "Senha1234");
        repository.salvar(usuario);

        for (int i = 0; i < 4; i++) {
            assertThrows(UsuarioValidationException.class,
                () -> authService.autenticar("joao@edu.unisinos.br", "senhaErrada"));
        }

        UsuarioValidationException exception = assertThrows(
            UsuarioValidationException.class,
            () -> authService.autenticar("joao@edu.unisinos.br", "senhaErrada")
        );

        assertEquals("Conta bloqueada por 15 minutos após 5 tentativas de login malsucedidas.", exception.getMessage());
    }

    @Test
    public void deveNegarAcessoQuandoContaBloqueada() {
        Usuario usuario = criarUsuario("lucas@edu.unisinos.br", "Senha1234");
        usuario.setTentativasFalhas(5);
        usuario.setBloqueadoAte(System.currentTimeMillis() + 15 * 60 * 1000L);
        repository.salvar(usuario);

        UsuarioValidationException exception = assertThrows(
            UsuarioValidationException.class,
            () -> authService.autenticar("lucas@edu.unisinos.br", "Senha1234")
        );

        assertEquals("Conta bloqueada. Tente novamente mais tarde.", exception.getMessage());
    }

    @Test
    public void deveResetarTentativasAposLoginBemSucedido() {
        Usuario usuario = criarUsuario("ana@edu.unisinos.br", "Senha1234");
        usuario.setTentativasFalhas(2);
        repository.salvar(usuario);

        authService.autenticar("ana@edu.unisinos.br", "Senha1234");

        Optional<Usuario> salvo = repository.buscarPorEmail("ana@edu.unisinos.br");
        assertEquals(0, salvo.get().getTentativasFalhas());
        assertEquals(null, salvo.get().getBloqueadoAte());
    }

    private Usuario criarUsuario(String email, String senha) {
        return new Usuario(null, "Teste", email, senha, TipoUsuario.PASSAGEIRO);
    }

    private static class InMemoryUsuarioRepository implements UsuarioRepository {

        private final Map<String, Usuario> usuarios = new HashMap<>();
        private long nextId = 1;

        @Override
        public boolean existsByEmail(String email) {
            if (email == null) {
                return false;
            }
            return usuarios.containsKey(email.trim().toLowerCase());
        }

        @Override
        public Usuario salvar(Usuario usuario) {
            String email = usuario.getEmail().trim().toLowerCase();
            Usuario salvo = new Usuario(
                nextId++,
                usuario.getNome(),
                email,
                usuario.getSenha(),
                usuario.getTipo(),
                usuario.getTentativasFalhas(),
                usuario.getBloqueadoAte()
            );
            usuarios.put(email, salvo);
            return salvo;
        }

        @Override
        public Optional<Usuario> buscarPorEmail(String email) {
            if (email == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(usuarios.get(email.trim().toLowerCase()));
        }

        @Override
        public void atualizarStatusDeBloqueio(String email, int tentativasFalhas, Long bloqueadoAte) {
            Optional<Usuario> usuario = buscarPorEmail(email);
            if (usuario.isPresent()) {
                Usuario atualizado = usuario.get();
                atualizado.setTentativasFalhas(tentativasFalhas);
                atualizado.setBloqueadoAte(bloqueadoAte);
                usuarios.put(email.trim().toLowerCase(), atualizado);
            }
        }
    }
}
