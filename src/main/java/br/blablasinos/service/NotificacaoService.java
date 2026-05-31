package br.blablasinos.service;

import br.blablasinos.model.Notificacao;
import br.blablasinos.repository.NotificacaoRepository;
import br.blablasinos.repository.SqliteNotificacaoRepository;
import java.util.List;

public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepo;

    public NotificacaoService() {
        this.notificacaoRepo = new SqliteNotificacaoRepository();
    }

    public NotificacaoService(NotificacaoRepository notificacaoRepo) {
        this.notificacaoRepo = notificacaoRepo;
    }

    public Notificacao notificar(Long usuarioId, String tipo, String mensagem, Long referenciaId) {
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuarioId(usuarioId);
        notificacao.setTipo(tipo);
        notificacao.setMensagem(mensagem);
        notificacao.setReferenciaId(referenciaId);
        notificacao.setLida(false);
        return notificacaoRepo.salvar(notificacao);
    }

    public List<Notificacao> listarPorUsuario(Long usuarioId) {
        return notificacaoRepo.listarPorUsuario(usuarioId);
    }

    public void marcarComoLida(Long usuarioId, Long notificacaoId) {
        Notificacao notificacao = notificacaoRepo.buscarPorId(notificacaoId)
            .orElseThrow(() -> new IllegalArgumentException("Notificacao nao encontrada."));

        if (!notificacao.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("Voce nao tem permissao para alterar esta notificacao.");
        }
        notificacaoRepo.marcarComoLida(notificacaoId);
    }
}
