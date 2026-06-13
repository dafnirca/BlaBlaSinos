package br.blablasinos.service;

import br.blablasinos.model.Avaliacao;
import br.blablasinos.model.Carona;
import br.blablasinos.model.Reserva;
import br.blablasinos.model.StatusCarona;
import br.blablasinos.repository.AvaliacaoRepository;
import br.blablasinos.repository.CaronaRepository;
import br.blablasinos.repository.ReservaRepository;
import br.blablasinos.repository.SqliteAvaliacaoRepository;
import br.blablasinos.repository.SqliteReservaRepository;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepo;
    private final CaronaRepository caronaRepo;
    private final ReservaRepository reservaRepo;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepo, CaronaRepository caronaRepo, ReservaRepository reservaRepo) {
        this.avaliacaoRepo = avaliacaoRepo;
        this.caronaRepo = caronaRepo;
        this.reservaRepo = reservaRepo;
    }

    public AvaliacaoService() {
        this(new SqliteAvaliacaoRepository(), new CaronaRepository("jdbc:sqlite:caronas.db"), new SqliteReservaRepository());
    }

    public Avaliacao avaliar(Long caronaId, Long avaliadorId, Long avaliadoId, int nota, String comentario)
            throws AvaliacaoException, SQLException {
        validarIds(caronaId, avaliadorId, avaliadoId);
        validarNota(nota);

        if (avaliadorId.equals(avaliadoId)) {
            throw new AvaliacaoException("Um usuário não pode avaliar a si mesmo.");
        }

        Carona carona = caronaRepo.buscarPorId(caronaId)
            .orElseThrow(() -> new AvaliacaoException("Carona não encontrada."));

        if (carona.getStatus() != StatusCarona.CONCLUIDA) {
            throw new AvaliacaoException("A carona precisa estar concluída para receber avaliações.");
        }

        List<Reserva> confirmadas = reservaRepo.listarConfirmadasPorCarona(caronaId);
        boolean avaliadorMotorista = carona.getMotoristaId().equals(avaliadorId);
        boolean avaliadoMotorista = carona.getMotoristaId().equals(avaliadoId);
        boolean avaliadorPassageiro = participouComoPassageiro(confirmadas, avaliadorId);
        boolean avaliadoPassageiro = participouComoPassageiro(confirmadas, avaliadoId);

        boolean motoristaAvaliaPassageiro = avaliadorMotorista && avaliadoPassageiro;
        boolean passageiroAvaliaMotorista = avaliadorPassageiro && avaliadoMotorista;

        if (!motoristaAvaliaPassageiro && !passageiroAvaliaMotorista) {
            throw new AvaliacaoException("Só é permitido avaliar usuários que participaram da carona.");
        }

        if (avaliacaoRepo.buscarPorCaronaEAvaliadorEAvaliado(caronaId, avaliadorId, avaliadoId).isPresent()) {
            throw new AvaliacaoException("Você já avaliou este usuário nesta carona.");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setIdCarona(caronaId);
        avaliacao.setIdAvaliador(avaliadorId);
        avaliacao.setIdAvaliado(avaliadoId);
        avaliacao.setNota(nota);
        avaliacao.setComentario(comentario == null ? "" : comentario.trim());
        avaliacao.setDataAvaliacao(LocalDateTime.now());
        return avaliacaoRepo.salvar(avaliacao);
    }

    public ResumoAvaliacoes resumoRecebidas(Long usuarioId) throws AvaliacaoException {
        if (usuarioId == null) {
            throw new AvaliacaoException("ID do usuário é obrigatório.");
        }

        List<Avaliacao> avaliacoes = avaliacaoRepo.listarRecebidasPorUsuario(usuarioId);
        double media = avaliacoes.stream().mapToInt(Avaliacao::getNota).average().orElse(0.0);
        return new ResumoAvaliacoes(media, avaliacoes.size(), avaliacoes);
    }

    public List<Long> listarAvaliaveis(Long caronaId, Long avaliadorId) throws AvaliacaoException, SQLException {
        if (caronaId == null || avaliadorId == null) {
            throw new AvaliacaoException("Carona e avaliador são obrigatórios.");
        }
        Carona carona = caronaRepo.buscarPorId(caronaId)
            .orElseThrow(() -> new AvaliacaoException("Carona não encontrada."));

        if (carona.getStatus() != StatusCarona.CONCLUIDA) {
            return List.of();
        }

        List<Reserva> confirmadas = reservaRepo.listarConfirmadasPorCarona(caronaId);
        List<Long> jaAvaliados = avaliacaoRepo.listarPorCaronaEAvaliador(caronaId, avaliadorId)
            .stream()
            .map(Avaliacao::getIdAvaliado)
            .toList();

        if (carona.getMotoristaId().equals(avaliadorId)) {
            return confirmadas.stream()
                .map(Reserva::getPassageiroId)
                .filter(id -> !jaAvaliados.contains(id))
                .toList();
        }

        if (participouComoPassageiro(confirmadas, avaliadorId) && !jaAvaliados.contains(carona.getMotoristaId())) {
            return List.of(carona.getMotoristaId());
        }

        return List.of();
    }

    private boolean participouComoPassageiro(List<Reserva> confirmadas, Long usuarioId) {
        return confirmadas.stream().anyMatch(r -> r.getPassageiroId().equals(usuarioId));
    }

    private void validarIds(Long caronaId, Long avaliadorId, Long avaliadoId) throws AvaliacaoException {
        if (caronaId == null || avaliadorId == null || avaliadoId == null) {
            throw new AvaliacaoException("Carona, avaliador e avaliado são obrigatórios.");
        }
    }

    private void validarNota(int nota) throws AvaliacaoException {
        if (nota < 1 || nota > 5) {
            throw new AvaliacaoException("A nota deve estar entre 1 e 5.");
        }
    }

    public static class ResumoAvaliacoes {
        private final double media;
        private final int quantidade;
        private final List<Avaliacao> comentarios;

        public ResumoAvaliacoes(double media, int quantidade, List<Avaliacao> comentarios) {
            this.media = media;
            this.quantidade = quantidade;
            this.comentarios = comentarios;
        }

        public double getMedia() { return media; }
        public int getQuantidade() { return quantidade; }
        public List<Avaliacao> getComentarios() { return comentarios; }
    }

    public static class AvaliacaoException extends Exception {
        public AvaliacaoException(String mensagem) { super(mensagem); }
    }
}
