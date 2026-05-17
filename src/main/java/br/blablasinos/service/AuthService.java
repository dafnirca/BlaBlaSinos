package br.blablasinos.service;

import br.blablasinos.exception.UsuarioValidationException;
import br.blablasinos.model.Usuario;
import br.blablasinos.repository.SqliteUsuarioRepository;
import br.blablasinos.repository.UsuarioRepository;

import java.time.Instant;

public class AuthService {

    private static final int MAX_TENTATIVAS = 5;
    private static final long BLOQUEIO_MILIS = 15 * 60 * 1000L;

    private final UsuarioRepository repository;

    public AuthService() {
        this(new SqliteUsuarioRepository());
    }

    public AuthService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new UsuarioValidationException("Credenciais inválidas.");
        }

        String emailNormalizado = email.trim().toLowerCase();
        Usuario usuario = repository.buscarPorEmail(emailNormalizado)
            .orElseThrow(() -> new UsuarioValidationException("Credenciais inválidas."));

        long agora = Instant.now().toEpochMilli();
        Long bloqueadoAte = usuario.getBloqueadoAte();

        if (bloqueadoAte != null && bloqueadoAte > agora) {
            throw new UsuarioValidationException("Conta bloqueada. Tente novamente mais tarde.");
        }

        if (bloqueadoAte != null && bloqueadoAte <= agora) {
            usuario.setTentativasFalhas(0);
            usuario.setBloqueadoAte(null);
        }

        if (!usuario.getSenha().equals(senha)) {
            int tentativas = usuario.getTentativasFalhas() + 1;
            if (tentativas >= MAX_TENTATIVAS) {
                long novoBloqueio = agora + BLOQUEIO_MILIS;
                repository.atualizarStatusDeBloqueio(emailNormalizado, tentativas, novoBloqueio);
                throw new UsuarioValidationException("Conta bloqueada por 15 minutos após 5 tentativas de login malsucedidas.");
            }

            repository.atualizarStatusDeBloqueio(emailNormalizado, tentativas, null);
            throw new UsuarioValidationException("Credenciais inválidas.");
        }

        if (usuario.getTentativasFalhas() > 0 || usuario.getBloqueadoAte() != null) {
            repository.atualizarStatusDeBloqueio(emailNormalizado, 0, null);
        }

        return usuario;
    }
}
