package br.blablasinos.model;

import java.time.LocalDateTime;

public class Carona {
    private Long id;
    private Long motoristaId;
    private String origem;
    private String destino;
    private LocalDateTime dataHora;
    private int vagasDisponiveis;
    private int vagasTotais;
    private double valor;
    private StatusCarona status = StatusCarona.AGENDADA;

    public Carona() {}

    public Carona(Long id, Long motoristaId, String origem, String destino, LocalDateTime dataHora,
                  int vagasDisponiveis, int vagasTotais) {
        this(id, motoristaId, origem, destino, dataHora, vagasDisponiveis, vagasTotais, 0.0);
    }

    public Carona(Long id, Long motoristaId, String origem, String destino, LocalDateTime dataHora,
                  int vagasDisponiveis, int vagasTotais, double valor) {
        this.id = id;
        this.motoristaId = motoristaId;
        this.origem = origem;
        this.destino = destino;
        this.dataHora = dataHora;
        this.vagasDisponiveis = vagasDisponiveis;
        this.vagasTotais = vagasTotais;
        this.valor = valor;
        this.status = StatusCarona.AGENDADA;
    }

    public Carona(Long id, Long motoristaId, String origem, String destino, LocalDateTime dataHora,
                  int vagasDisponiveis, int vagasTotais, double valor, StatusCarona status) {
        this(id, motoristaId, origem, destino, dataHora, vagasDisponiveis, vagasTotais, valor);
        this.status = status == null ? StatusCarona.AGENDADA : status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMotoristaId() { return motoristaId; }
    public void setMotoristaId(Long motoristaId) { this.motoristaId = motoristaId; }
    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public int getVagasDisponiveis() { return vagasDisponiveis; }
    public void setVagasDisponiveis(int vagasDisponiveis) { this.vagasDisponiveis = vagasDisponiveis; }
    public int getVagasTotais() { return vagasTotais; }
    public void setVagasTotais(int vagasTotais) { this.vagasTotais = vagasTotais; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public StatusCarona getStatus() { return status; }
    public void setStatus(StatusCarona status) { this.status = status == null ? StatusCarona.AGENDADA : status; }
}
