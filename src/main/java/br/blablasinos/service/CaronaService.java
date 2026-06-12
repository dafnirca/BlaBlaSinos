package br.blablasinos.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import br.blablasinos.model.Carona;
import br.blablasinos.model.Reserva;
import br.blablasinos.model.StatusCarona;
import br.blablasinos.model.TipoUsuario;
import br.blablasinos.model.Usuario;
import br.blablasinos.repository.CaronaRepository;
import br.blablasinos.repository.ReservaRepository;
import br.blablasinos.repository.SqliteReservaRepository;
import br.blablasinos.repository.SqliteUsuarioRepository;
import br.blablasinos.repository.UsuarioRepository;

public class CaronaService {

    public static final int VAGAS_MINIMAS = 1;
    public static final int VAGAS_MAXIMAS = 4;

    private static final List<String> CAMPUS_KEYWORDS = List.of(
        "são leopoldo", "sao leopoldo", "porto alegre", "poa", "são leo", "sao leo"
    );

    private final CaronaRepository  caronaRepo;
    private final UsuarioRepository usuarioRepo;
    private final ReservaRepository reservaRepo;
    private final NotificacaoService notificacaoService;

    // Construtor para testes
    public CaronaService(CaronaRepository caronaRepo, UsuarioRepository usuarioRepo, ReservaRepository reservaRepo) {
        this(caronaRepo, usuarioRepo, reservaRepo, new NotificacaoService());
    }

    public CaronaService(CaronaRepository caronaRepo, UsuarioRepository usuarioRepo, ReservaRepository reservaRepo, NotificacaoService notificacaoService) {
        this.caronaRepo  = caronaRepo;
        this.usuarioRepo = usuarioRepo;
        this.reservaRepo = reservaRepo;
        this.notificacaoService = notificacaoService;
    }

    // Construtor padrão que os Handlers usam
    public CaronaService() {
        this.caronaRepo = new CaronaRepository("jdbc:sqlite:caronas.db");
        this.usuarioRepo = new SqliteUsuarioRepository();
        this.reservaRepo = new SqliteReservaRepository();
        this.notificacaoService = new NotificacaoService();
    }


    public Carona cadastrarCarona(Long motoristaId,
                                  String origen,
                                  String destino,
                                  LocalDateTime dataHora,
                                  int vagasTotais)
            throws CaronaException, SQLException {
        return cadastrarCarona(motoristaId, origen, destino, dataHora, vagasTotais, 0.0);
    }

    public Carona cadastrarCarona(Long motoristaId,
                                  String origen,
                                  String destino,
                                  LocalDateTime dataHora,
                                  int vagasTotais,
                                  double valor)
            throws CaronaException, SQLException {

        Usuario motorista = buscarUsuario(motoristaId);
        validarPerfilMotorista(motorista);    
        validarCampus(origen, destino);      
        validarVagas(vagasTotais);          
        validarValor(valor);                
        validarDataHoraFutura(dataHora);      

        if (caronaRepo.existeConflitoHorario(motoristaId, dataHora)) {  
            throw new CaronaException(
                "Você já possui uma carona ativa neste horário. " +
                "Não é permitido oferecer caronas simultâneas (RN02.4).");
        }

        Carona carona = new Carona();
        carona.setMotoristaId(motoristaId);
        carona.setOrigem(origen.trim());
        carona.setDestino(destino.trim());
        carona.setDataHora(dataHora);
        carona.setVagasTotais(vagasTotais);
        carona.setVagasDisponiveis(vagasTotais);
        carona.setValor(valor);
        carona.setStatus(StatusCarona.AGENDADA);

        caronaRepo.salvar(carona);   
        return carona;
    }


    public Carona editarCarona(Long motoristaId,
                               Long caronaId,
                               String novaOrigem,
                               String novoDestino,
                               LocalDateTime novaDataHora,
                               int novasVagasTotais)
            throws CaronaException, SQLException {

        Carona carona = buscarCaronaDoMotorista(motoristaId, caronaId);

        validarEdicaoPermitida(carona);

        String origemEfetiva  = (novaOrigem  != null) ? novaOrigem  : carona.getOrigem();
        String destinoEfetivo = (novoDestino != null) ? novoDestino : carona.getDestino();
        validarCampus(origemEfetiva, destinoEfetivo);

        if (novaDataHora != null) {
            validarDataHoraFutura(novaDataHora);
        }

        if (novaDataHora != null &&
            caronaRepo.existeConflitoHorarioExcluindo(motoristaId, novaDataHora, caronaId)) {
            throw new CaronaException(
                "O novo horário conflita com outra carona ativa sua. " +
                "Não é permitido ter caronas simultâneas (RN02.4).");
        }

        if (novasVagasTotais >= 0) {
            validarVagas(novasVagasTotais);

            int vagasReservadas = carona.getVagasTotais() - carona.getVagasDisponiveis();
            if (novasVagasTotais < vagasReservadas) {
                throw new CaronaException(String.format(
                    "Não é possível reduzir as vagas para %d: há %d reserva(s) confirmada(s). " +
                    "Cancele reservas pendentes antes de reduzir as vagas.",
                    novasVagasTotais, vagasReservadas));
            }

            carona.setVagasDisponiveis(novasVagasTotais - vagasReservadas);
            carona.setVagasTotais(novasVagasTotais);
        }

        if (novaOrigem    != null) carona.setOrigem(novaOrigem.trim());
        if (novoDestino   != null) carona.setDestino(novoDestino.trim());
        if (novaDataHora  != null) carona.setDataHora(novaDataHora);

        caronaRepo.atualizarEdicao(carona);
        return carona;
    }

    public void cancelarCarona(Long motoristaId, Long caronaId)
            throws CaronaException, SQLException {

        Carona carona = buscarCaronaDoMotorista(motoristaId, caronaId);
        validarEdicaoPermitida(carona);   

        carona.setStatus(StatusCarona.CANCELADA);
        caronaRepo.atualizarStatus(caronaId, StatusCarona.CANCELADA);
    }


    public List<Carona> listarMinhasCaronas(Long motoristaId) throws SQLException {
        atualizarStatusCaronas();
        return caronaRepo.listarPorMotorista(motoristaId);
    }

    public List<Carona> buscarCaronasDisponiveis(String destino, String data) throws SQLException {
        atualizarStatusCaronas();
        return caronaRepo.listarAtivas(destino, data);
    }

    public Reserva solicitarVaga(Long passageiroId, Long caronaId) throws CaronaException, SQLException {
        buscarUsuario(passageiroId);
        Carona carona = caronaRepo.buscarPorId(caronaId)
            .orElseThrow(() -> new CaronaException("Carona não encontrada (id=" + caronaId + ")."));

        if (carona.getMotoristaId().equals(passageiroId)) {
            throw new CaronaException("O motorista não pode solicitar sua própria carona.");
        }

        if (carona.getVagasDisponiveis() <= 0) {
            throw new CaronaException("Não há vagas disponíveis nesta carona (RN04.1).");
        }

        Reserva novaReserva = new Reserva();
        novaReserva.setPassageiroId(passageiroId);
        novaReserva.setCaronaId(caronaId);
        novaReserva.setStatus("PENDENTE");

        Reserva reservaCriada = reservaRepo.salvar(novaReserva);
        notificarUsuario(
            carona.getMotoristaId(),
            "NOVA_SOLICITACAO",
            "Voce recebeu uma nova solicitacao para a carona #" + caronaId + ".",
            reservaCriada.getId()
        );
        return reservaCriada;
    }

    public List<Reserva> listarSolicitacoesPendentes(Long motoristaId) throws SQLException {
        return reservaRepo.listarPendentesPorMotorista(motoristaId);
    }

    public List<Reserva> listarSolicitacoesDoPassageiro(Long passageiroId) throws SQLException {
        return reservaRepo.listarPorPassageiro(passageiroId);
    }

    public Carona buscarCaronaPorId(Long caronaId) throws CaronaException, SQLException {
        atualizarStatusCaronas();
        return caronaRepo.buscarPorId(caronaId)
                .orElseThrow(() -> new CaronaException("Carona não encontrada (id=" + caronaId + ")."));
    }

    public Reserva decidirSolicitacao(Long motoristaId, Long reservaId, boolean aceitar)
            throws CaronaException, SQLException {
        Reserva reserva = reservaRepo.buscarPorId(reservaId)
            .orElseThrow(() -> new CaronaException("Reserva não encontrada (id=" + reservaId + ")."));

        Carona carona = buscarCaronaDoMotorista(motoristaId, reserva.getCaronaId());

        if (!"PENDENTE".equals(reserva.getStatus())) {
            throw new CaronaException("Esta solicitação já foi decidida.");
        }

        if (aceitar) {
            if (carona.getVagasDisponiveis() <= 0) {
                throw new CaronaException("Não há vagas disponíveis para confirmar esta solicitação.");
            }
            carona.setVagasDisponiveis(carona.getVagasDisponiveis() - 1);
            carona.setStatus(StatusCarona.AGENDADA);
            caronaRepo.atualizar(carona);
            reserva.setStatus("CONFIRMADA");
        } else {
            reserva.setStatus("CANCELADA");
        }

        reservaRepo.update(reserva);
        String tipo = aceitar ? "SOLICITACAO_ACEITA" : "SOLICITACAO_RECUSADA";
        String mensagem = aceitar
            ? "Sua solicitacao da carona #" + carona.getId() + " foi aceita."
            : "Sua solicitacao da carona #" + carona.getId() + " foi recusada.";
        notificarUsuario(reserva.getPassageiroId(), tipo, mensagem, reserva.getId());
        return reserva;
    }

    private void notificarUsuario(Long usuarioId, String tipo, String mensagem, Long referenciaId) {
        notificacaoService.notificar(usuarioId, tipo, mensagem, referenciaId);
    }

    public void cancelarSolicitacao(Long passageiroId, Long reservaId) throws CaronaException, SQLException {
        Reserva reserva = reservaRepo.buscarPorId(reservaId)
            .orElseThrow(() -> new CaronaException("Reserva não encontrada (id=" + reservaId + ")."));

        if (!reserva.getPassageiroId().equals(passageiroId)) {
            throw new CaronaException("Você não tem permissão para cancelar esta solicitação.");
        }

        if ("CONFIRMADA".equals(reserva.getStatus())) {
            Carona carona = caronaRepo.buscarPorId(reserva.getCaronaId())
                .orElseThrow(() -> new CaronaException("Carona associada à reserva não foi encontrada."));

            carona.setVagasDisponiveis(carona.getVagasDisponiveis() + 1);
            caronaRepo.atualizar(carona);
        }

        reservaRepo.deletar(reservaId);
    }

    public void atualizarStatusCaronas() throws SQLException {
        caronaRepo.ativarAgendadasVencidasComReservaConfirmada(LocalDateTime.now());
    }

    public Carona concluirCarona(Long motoristaId, Long caronaId) throws CaronaException, SQLException {
        atualizarStatusCaronas();
        Carona carona = buscarCaronaDoMotorista(motoristaId, caronaId);

        if (carona.getStatus() != StatusCarona.ATIVA) {
            throw new CaronaException("Apenas caronas ativas podem ser marcadas como concluídas.");
        }

        carona.setStatus(StatusCarona.CONCLUIDA);
        caronaRepo.atualizarStatus(caronaId, StatusCarona.CONCLUIDA);
        return carona;
    }

    private void validarEdicaoPermitida(Carona carona) throws CaronaException {
        if (carona.getStatus() == StatusCarona.CONCLUIDA || carona.getStatus() == StatusCarona.CANCELADA) {
            throw new CaronaException("Não é possível editar ou cancelar uma carona encerrada.");
        }
        if (!LocalDateTime.now().isBefore(carona.getDataHora())) {
            throw new CaronaException(
                "Não é possível editar ou cancelar uma carona após o horário de saída (RN02.6).");
        }
    }

    private void validarCampus(String origem, String destino) throws CaronaException {
        if (contemUnisinosSemCampus(origem) || contemUnisinosSemCampus(destino)) {
            throw new CaronaException(
                "A origem ou o destino deve informar um campus real " +
                "(São Leopoldo ou Porto Alegre) e não apenas 'Unisinos' (RN02.1).");
        }

        if (!contemCampus(origem) && !contemCampus(destino)) {
            throw new CaronaException(
                "A origem ou o destino deve ser um campus Unisinos " +
                "(São Leopoldo ou Porto Alegre) (RN02.1).");
        }
    }

    private boolean contemUnisinosSemCampus(String local) {
        if (local == null) return false;
        String n = local.toLowerCase().trim();
        return n.contains("unisinos") && !contemCampus(n);
    }

    private boolean contemCampus(String local) {
        if (local == null) return false;
        String n = local.toLowerCase().trim();
        return CAMPUS_KEYWORDS.stream().anyMatch(n::contains);
    }

    private void validarValor(double valor) throws CaronaException {
        if (valor < 0) {
            throw new CaronaException("O valor da carona não pode ser negativo.");
        }
    }

    private void validarVagas(int vagas) throws CaronaException {
        if (vagas < VAGAS_MINIMAS || vagas > VAGAS_MAXIMAS) {
            throw new CaronaException(String.format(
                "O número de vagas deve ser entre %d e %d (RN02.2). Informado: %d",
                VAGAS_MINIMAS, VAGAS_MAXIMAS, vagas));
        }
    }

    private void validarDataHoraFutura(LocalDateTime dataHora) throws CaronaException {
        if (dataHora == null || !dataHora.isAfter(LocalDateTime.now())) {
            throw new CaronaException(
                "O horário de saída deve ser posterior ao momento atual (RN02.3).");
        }
    }

    private void validarPerfilMotorista(Usuario usuario) throws CaronaException {
        if (usuario.getTipo() != TipoUsuario.MOTORISTA) {
            throw new CaronaException(
                "Apenas usuários com perfil Motorista podem oferecer caronas (RN02.5).");
        }
        boolean veiculoCompleto = usuario.getCnh()           != null && !usuario.getCnh().isBlank()
                               && usuario.getModeloVeiculo() != null && !usuario.getModeloVeiculo().isBlank()
                               && usuario.getCorVeiculo()    != null && !usuario.getCorVeiculo().isBlank()
                               && usuario.getPlacaVeiculo()  != null && !usuario.getPlacaVeiculo().isBlank();
        if (!veiculoCompleto) {
            throw new CaronaException(
                "Seu perfil de motorista está incompleto. " +
                "Cadastre CNH, modelo, cor e placa antes de oferecer caronas (RN02.5).");
        }
    }

    private Carona buscarCaronaDoMotorista(Long motoristaId, Long caronaId)
            throws CaronaException, SQLException {

        Carona carona = caronaRepo.buscarPorId(caronaId)
            .orElseThrow(() -> new CaronaException(
                "Carona não encontrada (id=" + caronaId + ")."));

        if (!carona.getMotoristaId().equals(motoristaId)) {
            throw new CaronaException(
                "Você não tem permissão para gerenciar esta carona.");
        }
        return carona;
    }

    
    private Usuario buscarUsuario(Long id) throws CaronaException {
        if (id == null) {
            throw new CaronaException("ID do usuário inválido.");
        }
        
        // Use buscarPorId para localizar o usuário por ID
        return usuarioRepo.buscarPorId(id)
            .orElseThrow(() -> new CaronaException(
                "Usuário não encontrado (id=" + id + ")."));
    }

    public static class CaronaException extends Exception {
        public CaronaException(String mensagem) { super(mensagem); }
    }
}
