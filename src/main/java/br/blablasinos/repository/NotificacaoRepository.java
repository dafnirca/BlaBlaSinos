package br.blablasinos.repository;

import br.blablasinos.model.Notificacao;
import java.util.List;
import java.util.Optional;

public interface NotificacaoRepository {
    Notificacao salvar(Notificacao notificacao);
    List<Notificacao> listarPorUsuario(long usuarioId);
    Optional<Notificacao> buscarPorId(long id);
    void marcarComoLida(long id);
}
