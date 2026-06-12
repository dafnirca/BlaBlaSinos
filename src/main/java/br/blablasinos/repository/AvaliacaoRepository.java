package br.blablasinos.repository;

import br.blablasinos.model.Avaliacao;
import java.util.List;
import java.util.Optional;

public interface AvaliacaoRepository {
    Avaliacao salvar(Avaliacao avaliacao);
    Optional<Avaliacao> buscarPorCaronaEAvaliadorEAvaliado(long caronaId, long avaliadorId, long avaliadoId);
    List<Avaliacao> listarRecebidasPorUsuario(long usuarioId);
    List<Avaliacao> listarPorCaronaEAvaliador(long caronaId, long avaliadorId);
}
