package br.blablasinos.repository;

import br.blablasinos.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository {
    boolean existsByEmail(String email);
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorEmail(String email);
    void atualizarStatusDeBloqueio(String email, int tentativasFalhas, Long bloqueadoAte);
    
    void update(Usuario usuario); 
}