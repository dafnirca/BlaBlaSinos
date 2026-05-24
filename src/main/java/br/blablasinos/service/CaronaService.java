package br.blablasinos.service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import br.blablasinos.model.Carona;
import br.blablasinos.model.Reserva;
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
        "unisinos", "são leopoldo", "sao leopoldo", "porto alegre"
    );

    private final CaronaRepository  caronaRepo;
    private final UsuarioRepository usuarioRepo;
    private final ReservaRepository reservaRepo;

    // Construtor para testes
    public CaronaService(CaronaRepository caronaRepo, UsuarioRepository usuarioRepo, ReservaRepository reservaRepo) {
        this.caronaRepo  = caronaRepo;
        this.usuarioRepo = usuarioRepo;
        this.reservaRepo = reservaRepo;
    }

    // Construtor padrão que os Handlers usam
    public CaronaService() {
        this.caronaRepo = new CaronaRepository("jdbc:sqlite:caronas.db");
        this.usuarioRepo = new SqliteUsuarioRepository();
        this.reservaRepo = new SqliteReservaRepository();
    }


    public Carona cadastrarCarona(Long motoristaId,
                                  String origen,
                                  String destino,
                                  LocalDateTime dataHora,
                                  int vagasTotais)
            throws CaronaException, SQLException {

        Usuario motorista = buscarUsuario(motoristaId);
        validarPerfilMotorista(motorista);    
        validarCampus(origen, destino);      
        validarVagas(vagasTotais);          
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

        caronaRepo.deletar(caronaId);
    }


    public List<Carona> listarMinhasCaronas(Long motoristaId) throws SQLException {
        return caronaRepo.listarPorMotorista(motoristaId);
    }

    public List<Carona> buscarCaronasDisponiveis(String destino, String data) throws SQLException {
        return caronaRepo.listarAtivas(destino, data);
    }

    public Reserva solicitarVaga(Long passageiroId, Long caronaId) throws CaronaException, SQLException {
        buscarUsuario(passageiroId);
        Carona carona = caronaRepo.buscarPorId(caronaId)
            .orElseThrow(() -> new CaronaException("Carona não encontrada (id=" + caronaId + ")."));

        if (carona.getVagasDisponiveis() <= 0) {
            throw new CaronaException("Não há vagas disponíveis nesta carona (RN04.1).");
        }
        
        carona.setVagasDisponiveis(carona.getVagasDisponiveis() - 1);
        caronaRepo.atualizar(carona);

        Reserva novaReserva = new Reserva();
        novaReserva.setPassageiroId(passageiroId);
        novaReserva.setCaronaId(caronaId);
        novaReserva.setStatus("PENDENTE");

        return reservaRepo.salvar(novaReserva);
    }

    public void cancelarSolicitacao(Long passageiroId, Long reservaId) throws CaronaException, SQLException {
        Reserva reserva = reservaRepo.buscarPorId(reservaId)
            .orElseThrow(() -> new CaronaException("Reserva não encontrada (id=" + reservaId + ")."));

        if (!reserva.getPassageiroId().equals(passageiroId)) {
            throw new CaronaException("Você não tem permissão para cancelar esta solicitação.");
        }

        Carona carona = caronaRepo.buscarPorId(reserva.getCaronaId())
            .orElseThrow(() -> new CaronaException("Carona associada à reserva não foi encontrada."));

        carona.setVagasDisponiveis(carona.getVagasDisponiveis() + 1);
        caronaRepo.atualizar(carona);

        reservaRepo.deletar(reservaId);
    }

    private void validarEdicaoPermitida(Carona carona) throws CaronaException {
        if (!LocalDateTime.now().isBefore(carona.getDataHora())) {
            throw new CaronaException(
                "Não é possível editar ou cancelar uma carona após o horário de saída (RN02.6).");
        }
    }

    private void validarCampus(String origem, String destino) throws CaronaException {
        if (!contemCampus(origem) && !contemCampus(destino)) {
            throw new CaronaException(
                "A origem ou o destino deve ser um campus Unisinos " +
                "(São Leopoldo ou Porto Alegre) (RN02.1).");
        }
    }

    private boolean contemCampus(String local) {
        if (local == null) return false;
        String n = local.toLowerCase().trim();
        return CAMPUS_KEYWORDS.stream().anyMatch(n::contains);
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
        
        // ==================================================================
        // === ESTA É A CORREÇÃO FINAL E DEFINITIVA ===
        // ==================================================================
        // O código antigo estava chamando 'buscarPorEmail(id.toString())', o que estava errado.
        // O código correto é chamar 'buscarPorId(id)'.
        return usuarioRepo.buscarPorId(id)
            .orElseThrow(() -> new CaronaException(
                "Usuário não encontrado (id=" + id + ")."));
    }

    public static class CaronaException extends Exception {
        public CaronaException(String mensagem) { super(mensagem); }
    }
}