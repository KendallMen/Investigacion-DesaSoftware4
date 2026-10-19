package cr.ac.ucr.ms_reservas.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cr.ac.ucr.ms_reservas.model.Reserva;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByUsuarioId(Long usuarioId);

    @Query("""
            SELECT COUNT(r) FROM Reserva r
            WHERE r.laboratorioId = :labId
              AND r.fecha = :fecha
              AND r.estado = 'ACTIVA'
              AND r.horaInicio < :fin
              AND r.horaFin > :inicio
            """)
    long contarCruces(@Param("labId") Long labId,
                      @Param("fecha") LocalDate fecha,
                      @Param("inicio") LocalTime inicio,
                      @Param("fin") LocalTime fin);
}