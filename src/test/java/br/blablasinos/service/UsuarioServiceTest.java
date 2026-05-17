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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UsuarioServiceTest {

    private UsuarioService usuarioService;
    private InMemoryUsuarioRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new InMemoryUsuarioRepository();
        usuarioService = new UsuarioService(repository);
    }

    @Test
    public void deveCadastrarUsuarioValido() {
        Usuario usuario = new Usuario(
            null,
            "João Pedro",
            "joao.pedro@edu.unisinos.br",
            "senha1234",
            TipoUsuario.PASSAGEIRO
        );

        Usuario resultado = usuarioService.cadastrar(usuario);

        assertNotNull(resultado.getId());
        assertEquals("joao.pedro@edu.unisinos.br", resultado.getEmail());
        assertEquals("João Pedro", resultado.getNome());
    }

    @Test
    public void deveReprovarEmailJaCadastrado() {
        Usuario usuario1 = new Usuario(
            null,
            "Laura",
            "laura@edu.unisinos.br",
            "Senha1234",
            TipoUsuario.PASSAGEIRO
        );

        Usuario usuario2 = new Usuario(
            null,
            "Laura Silva",
            "LAURA@edu.unisinos.br",
            "Senha1234",
            TipoUsuario.MOTORISTA
        );

        usuarioService.cadastrar(usuario1);

        UsuarioValidationException exception = assertThrows(
            UsuarioValidationException.class,
            () -> usuarioService.cadastrar(usuario2)
        );

        assertEquals("O e-mail já está cadastrado.", exception.getMessage());
    }

    @Test
    public void deveReprovarEmailInstitucionalInvalido() {
        Usuario usuario = new Usuario(
            null,
            "Bruno",
            "bruno@gmail.com",
            "Senha1234",
            TipoUsuario.PASSAGEIRO
        );

        UsuarioValidationException exception = assertThrows(
            UsuarioValidationException.class,
            () -> usuarioService.cadastrar(usuario)
        );

        assertEquals("E-mail deve ser institucional com domínio @edu.unisinos.br.", exception.getMessage());
    }

    private static class InMemoryUsuarioRepository implements UsuarioRepository {

        private final Map<String, Usuario> usuariosPorEmail = new HashMap<>();
        private long nextId = 1;

        @Override
        public boolean existsByEmail(String email) {
            if (email == null) {
                return false;
            }
            return usuariosPorEmail.containsKey(email.trim().toLowerCase());
        }

        @Override
        public Usuario salvar(Usuario usuario) {
            String email = usuario.getEmail().trim().toLowerCase();
            Usuario salvo = new Usuario(
                nextId++,
                usuario.getNome(),
                email,
                usuario.getSenha(),
                usuario.getTipo()
            );
            usuariosPorEmail.put(email, salvo);
            return salvo;
        }

        @Override
        public Optional<Usuario> buscarPorEmail(String email) {
            if (email == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(usuariosPorEmail.get(email.trim().toLowerCase()));
        }
    }
}
