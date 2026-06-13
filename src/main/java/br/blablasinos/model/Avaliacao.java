package br.blablasinos.model;

import java.time.LocalDateTime;

public class Avaliacao {
    private Long id;
    private Long idCarona;
    private Long idAvaliador;
    private Long idAvaliado;
    private int nota;
    private String comentario;
    private LocalDateTime dataAvaliacao;

    public Avaliacao() {}

    public Avaliacao(Long id, Long idCarona, Long idAvaliador, Long idAvaliado,
                     int nota, String comentario, LocalDateTime dataAvaliacao) {
        this.id = id;
        this.idCarona = idCarona;
        this.idAvaliador = idAvaliador;
        this.idAvaliado = idAvaliado;
        this.nota = nota;
        this.comentario = comentario;
        this.dataAvaliacao = dataAvaliacao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdCarona() { return idCarona; }
    public void setIdCarona(Long idCarona) { this.idCarona = idCarona; }
    public Long getIdAvaliador() { return idAvaliador; }
    public void setIdAvaliador(Long idAvaliador) { this.idAvaliador = idAvaliador; }
    public Long getIdAvaliado() { return idAvaliado; }
    public void setIdAvaliado(Long idAvaliado) { this.idAvaliado = idAvaliado; }
    public int getNota() { return nota; }
    public void setNota(int nota) { this.nota = nota; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDateTime dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
}
