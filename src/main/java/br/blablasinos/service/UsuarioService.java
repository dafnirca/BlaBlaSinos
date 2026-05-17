package br.blablasinos.service;

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
}
