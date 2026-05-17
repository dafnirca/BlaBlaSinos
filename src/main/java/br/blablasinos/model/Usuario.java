package br.blablasinos.model;

public class Usuario {

    private Long id;
    private String nome;
    private String email;
    private String senha;
    private TipoUsuario tipo;
    private int tentativasFalhas;
    private Long bloqueadoAte;

    private String cnh;
    private String modeloVeiculo;
    private String corVeiculo;
    private String placaVeiculo;

    public Usuario(Long id, String nome, String email, String senha, TipoUsuario tipo, int tentativasFalhas, Long bloqueadoAte) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
        this.tentativasFalhas = tentativasFalhas;
        this.bloqueadoAte = bloqueadoAte;
    }
    

    public Usuario(Long id, String nome, String email, String senha, TipoUsuario tipo) {
        this(id, nome, email, senha, tipo, 0, null);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public TipoUsuario getTipo() { return tipo; }
    public void setTipo(TipoUsuario tipo) { this.tipo = tipo; }
    public int getTentativasFalhas() { return tentativasFalhas; }
    public void setTentativasFalhas(int tentativasFalhas) { this.tentativasFalhas = tentativasFalhas; }
    public Long getBloqueadoAte() { return bloqueadoAte; }
    public void setBloqueadoAte(Long bloqueadoAte) { this.bloqueadoAte = bloqueadoAte; }

    public String getCnh() { return cnh; }
    public void setCnh(String cnh) { this.cnh = cnh; }
    public String getModeloVeiculo() { return modeloVeiculo; }
    public void setModeloVeiculo(String modeloVeiculo) { this.modeloVeiculo = modeloVeiculo; }
    public String getCorVeiculo() { return corVeiculo; }
    public void setCorVeiculo(String corVeiculo) { this.corVeiculo = corVeiculo; }
    public String getPlacaVeiculo() { return placaVeiculo; }
    public void setPlacaVeiculo(String placaVeiculo) { this.placaVeiculo = placaVeiculo; }
}