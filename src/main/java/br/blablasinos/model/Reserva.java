package br.blablasinos.model;

public class Reserva {
    private Long id;
    private Long caronaId;
    private Long passageiroId;
    private String status;

    public Reserva() {}

    public Reserva(Long id, Long caronaId, Long passageiroId, String status) {
        this.id = id;
        this.caronaId = caronaId;
        this.passageiroId = passageiroId;
        this.status = status;
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCaronaId() { return caronaId; }
    public void setCaronaId(Long caronaId) { this.caronaId = caronaId; }
    public Long getPassageiroId() { return passageiroId; }
    public void setPassageiroId(Long passageiroId) { this.passageiroId = passageiroId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}