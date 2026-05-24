package br.blablasinos.repository;

import br.blablasinos.model.Reserva;
import java.util.Optional;

public interface ReservaRepository {
    Reserva salvar(Reserva reserva);
    Optional<Reserva> buscarPorId(long id);
    void deletar(long id);
}