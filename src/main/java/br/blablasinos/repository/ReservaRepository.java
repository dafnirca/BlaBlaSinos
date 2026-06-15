package br.blablasinos.repository;

import br.blablasinos.model.Reserva;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository {
    Reserva salvar(Reserva reserva);
    Optional<Reserva> buscarPorId(long id);
    void update(Reserva reserva);
    void deletar(long id);
    List<Reserva> listarPendentesPorMotorista(long motoristaId);
    List<Reserva> listarPorPassageiro(long passageiroId);
    List<Reserva> listarConfirmadasPorCarona(long caronaId);
}
