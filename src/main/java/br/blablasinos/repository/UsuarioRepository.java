package br.blablasinos.repository;

import br.blablasinos.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository {

    boolean existsByEmail(String email);

    Usuario salvar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);
}
