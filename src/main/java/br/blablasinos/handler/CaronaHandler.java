package br.blablasinos.handler;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import br.blablasinos.model.Carona;
import br.blablasinos.service.CaronaService;
import br.blablasinos.service.CaronaService.CaronaException;


public class CaronaHandler {

    private static final DateTimeFormatter FMT_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final CaronaService caronaService;

    private Long motoristaIdSessao;

    public CaronaHandler(CaronaService caronaService) {
        this.caronaService = caronaService;
    }


    public void iniciarSessao(Long motoristaId) {
        this.motoristaIdSessao = motoristaId;
    }

    public void encerrarSessao() {
        this.motoristaIdSessao = null;
    }


    public ResultadoOperacao<Carona> cadastrarCarona(String origemRaw,
                                                     String destinoRaw,
                                                     String dataRaw,
                                                     String horaRaw,
                                                     String vagasRaw,
                                                     String observacoesRaw) {
        if (motoristaIdSessao == null) {
            return ResultadoOperacao.falha("Nenhum usuário autenticado. Faça login novamente.");
        }

        LocalDateTime horarioSaida;
        int vagas;

        try {
            LocalDate data = LocalDate.parse(dataRaw.trim(), FMT_DATA);
            LocalTime hora = LocalTime.parse(horaRaw.trim(), FMT_HORA);
            horarioSaida = LocalDateTime.of(data, hora);
        } catch (DateTimeParseException e) {
            return ResultadoOperacao.falha(
                "Data ou hora inválida. Use o formato DD/MM/AAAA para data e HH:MM para hora.");
        }

        try {
            vagas = Integer.parseInt(vagasRaw.trim());
        } catch (NumberFormatException e) {
            return ResultadoOperacao.falha("Número de vagas inválido. Informe um valor entre 1 e 4.");
        }

        try {
            Carona carona = caronaService.cadastrarCarona(
                motoristaIdSessao,
                origemRaw,
                destinoRaw,
                horarioSaida,
                vagas
            );
            return ResultadoOperacao.sucesso(
                "Carona cadastrada com sucesso! (id: " + carona.getId() + ")",
                carona
            );

        } catch (CaronaException | IllegalArgumentException e) {
            return ResultadoOperacao.falha(e.getMessage());

        } catch (SQLException e) {
            return ResultadoOperacao.falha(
                "Erro ao salvar a carona. Tente novamente ou contate o suporte.");
        }
    }


    public ResultadoOperacao<Void> cancelarCarona(String caronaIdRaw) {
        if (motoristaIdSessao == null) {
            return ResultadoOperacao.falha("Nenhum usuário autenticado. Faça login novamente.");
        }

        long caronaId;
        try {
            caronaId = Long.parseLong(caronaIdRaw.trim());
        } catch (NumberFormatException e) {
            return ResultadoOperacao.falha("Identificador de carona inválido.");
        }

        try {
            caronaService.cancelarCarona(motoristaIdSessao, caronaId);
            return ResultadoOperacao.sucesso("Carona cancelada com sucesso.", null);

        } catch (CaronaException e) {
            return ResultadoOperacao.falha(e.getMessage());
        } catch (SQLException e) {
            return ResultadoOperacao.falha("Erro ao cancelar a carona. Tente novamente.");
        }
    }


    public ResultadoOperacao<List<Carona>> listarMinhasCaronas() {
        if (motoristaIdSessao == null) {
            return ResultadoOperacao.falha("Nenhum usuário autenticado.");
        }
        try {
            List<Carona> caronas = caronaService.listarMinhasCaronas(motoristaIdSessao);
            return ResultadoOperacao.sucesso("OK", caronas);
        } catch (SQLException e) {
            return ResultadoOperacao.falha("Erro ao carregar suas caronas. Tente novamente.");
        }
    }

    public ResultadoOperacao<List<Carona>> buscarCaronas(String destinoRaw, String dataRaw) {
        String destino = (destinoRaw != null && !destinoRaw.isBlank()) ? destinoRaw.trim() : null;
        String data    = null;

        if (dataRaw != null && !dataRaw.isBlank()) {
            try {
                LocalDate d = LocalDate.parse(dataRaw.trim(), FMT_DATA);
                data = d.toString();  
            } catch (DateTimeParseException e) {
                return ResultadoOperacao.falha(
                    "Data inválida. Use o formato DD/MM/AAAA.");
            }
        }

        try {
            List<Carona> caronas = caronaService.buscarCaronasDisponiveis(destino, data);
            String msg = caronas.isEmpty()
                ? "Nenhuma carona disponível para os filtros informados."
                : caronas.size() + " carona(s) encontrada(s).";
            return ResultadoOperacao.sucesso(msg, caronas);
        } catch (SQLException e) {
            return ResultadoOperacao.falha("Erro ao buscar caronas. Tente novamente.");
        }
    }


    public static class ResultadoOperacao<T> {

        private final boolean sucesso;
        private final String  mensagem;
        private final T       dado;

        private ResultadoOperacao(boolean sucesso, String mensagem, T dado) {
            this.sucesso  = sucesso;
            this.mensagem = mensagem;
            this.dado     = dado;
        }

        public static <T> ResultadoOperacao<T> sucesso(String message, T data) {
            return new ResultadoOperacao<>(true, message, data);
        }

        public static <T> ResultadoOperacao<T> falha(String message) {
            return new ResultadoOperacao<>(false, message, null);
        }

        public boolean isSucesso()  { return sucesso; }
        public String  getMensagem(){ return mensagem; }
        public T       getDado()    { return dado; }

        @Override
        public String toString() {
            return (sucesso ? "[OK] " : "[ERRO] ") + mensagem;
        }
    }
}