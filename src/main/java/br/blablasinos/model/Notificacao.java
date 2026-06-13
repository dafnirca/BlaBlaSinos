package br.blablasinos.model;

public class Notificacao {
    private Long id;
    private Long usuarioId;
    private String tipo;
    private String mensagem;
    private Long referenciaId;
    private boolean lida;
    private String criadaEm;

    public Notificacao() {}

    public Notificacao(Long id, Long usuarioId, String tipo, String mensagem, Long referenciaId, boolean lida, String criadaEm) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.referenciaId = referenciaId;
        this.lida = lida;
        this.criadaEm = criadaEm;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public Long getReferenciaId() { return referenciaId; }
    public void setReferenciaId(Long referenciaId) { this.referenciaId = referenciaId; }
    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
    public String getCriadaEm() { return criadaEm; }
    public void setCriadaEm(String criadaEm) { this.criadaEm = criadaEm; }
}
