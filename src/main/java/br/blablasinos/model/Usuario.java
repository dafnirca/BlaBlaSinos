package br.blablasinos.model;

public class Usuario {

    private Long id;

    private String nome;

    private String email;

    private String senha;

    private TipoUsuario tipo;

    private int tentativasFalhas;

    private Long bloqueadoAte;

    public Usuario() {
    }

    public Usuario(
        Long id,
        String nome,
        String email,
        String senha,
        TipoUsuario tipo
    ) {

        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
        this.tentativasFalhas = 0;
        this.bloqueadoAte = null;
    }

    public Usuario(
        Long id,
        String nome,
        String email,
        String senha,
        TipoUsuario tipo,
        int tentativasFalhas,
        Long bloqueadoAte
    ) {

        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
        this.tentativasFalhas = tentativasFalhas;
        this.bloqueadoAte = bloqueadoAte;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        this.tipo = tipo;
    }

    public int getTentativasFalhas() {
        return tentativasFalhas;
    }

    public void setTentativasFalhas(int tentativasFalhas) {
        this.tentativasFalhas = tentativasFalhas;
    }

    public Long getBloqueadoAte() {
        return bloqueadoAte;
    }

    public void setBloqueadoAte(Long bloqueadoAte) {
        this.bloqueadoAte = bloqueadoAte;
    }
}