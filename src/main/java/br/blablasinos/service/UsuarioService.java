package br.blablasinos.service;
import java.util.Optional;

import br.blablasinos.exception.UsuarioValidationException;
import br.blablasinos.model.Usuario;
import br.blablasinos.repository.SqliteUsuarioRepository;
import br.blablasinos.repository.UsuarioRepository;
import br.blablasinos.validation.UsuarioValidator;

public class UsuarioService {

    private final UsuarioRepository repository;

    public UsuarioService() {
        this(new SqliteUsuarioRepository());
    }

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public Usuario cadastrar(Usuario usuario) {
        UsuarioValidator.validar(usuario);

        String emailNormalizado = usuario.getEmail().trim().toLowerCase();
        usuario.setEmail(emailNormalizado);
        usuario.setNome(usuario.getNome().trim());

        if (repository.existsByEmail(emailNormalizado)) {
            throw new UsuarioValidationException("O e-mail já está cadastrado.");
        }

        return repository.salvar(usuario);
    }

    public void atualizarPerfil(Usuario usuario) throws UsuarioValidationException {
        // No futuro, o UsuarioValidator pode ter um método para validar os campos do perfil
        // UsuarioValidator.validarPerfil(usuario);

        if (usuario.getId() == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo para uma atualização.");
        }
        
        repository.update(usuario); 
    }

    public Optional<Usuario> buscarPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID do usuário não pode ser nulo.");
        }
        return repository.buscarPorId(id);
    }
}
