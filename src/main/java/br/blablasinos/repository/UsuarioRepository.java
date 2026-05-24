package br.blablasinos.repository;

import java.util.Optional;

import br.blablasinos.model.Usuario;

public interface UsuarioRepository {
    boolean existsByEmail(String email);
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorId(Long id);
    void atualizarStatusDeBloqueio(String email, int tentativasFalhas, Long bloqueadoAte);
    void update(Usuario usuario); 
}