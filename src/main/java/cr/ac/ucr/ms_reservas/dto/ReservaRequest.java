package cr.ac.ucr.ms_reservas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record ReservaRequest(
        @NotNull Long usuarioId,
        @NotNull Long laboratorioId,
        @NotNull LocalDate fecha,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin) {}