package cr.ac.ucr.ms_reservas.service;

import java.time.Duration;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cr.ac.ucr.ms_reservas.client.LaboratorioClient;
import cr.ac.ucr.ms_reservas.client.UsuarioClient;
import cr.ac.ucr.ms_reservas.config.ReservasProperties;
import cr.ac.ucr.ms_reservas.dto.LaboratorioDTO;
import cr.ac.ucr.ms_reservas.dto.ReservaRequest;
import cr.ac.ucr.ms_reservas.excepcion.ApiException;
import cr.ac.ucr.ms_reservas.model.Reserva;
import cr.ac.ucr.ms_reservas.repository.ReservaRepository;

@Service
public class ReservaService {

    private final ReservaRepository repo;
    private final UsuarioClient usuarioClient;
    private final LaboratorioClient laboratorioClient;
    private final ReservasProperties props;

    public ReservaService(ReservaRepository repo, UsuarioClient usuarioClient,
                          LaboratorioClient laboratorioClient, ReservasProperties props) {
        this.repo = repo;
        this.usuarioClient = usuarioClient;
        this.laboratorioClient = laboratorioClient;
        this.props = props;
    }

    @Transactional
    public Reserva crear(ReservaRequest req) {
        if (!req.horaFin().isAfter(req.horaInicio())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La hora de fin debe ser posterior a la hora de inicio");
        }

        long minutos = Duration.between(req.horaInicio(), req.horaFin()).toMinutes();
        int max = props.getMaxHoras();
        if (minutos > max * 60L) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La reserva no puede superar las " + max + " horas");
        }

        usuarioClient.obtener(req.usuarioId());
        LaboratorioDTO lab = laboratorioClient.obtener(req.laboratorioId());
        if (Boolean.FALSE.equals(lab.disponible())) {
            throw new ApiException(HttpStatus.CONFLICT, "El laboratorio no está disponible");
        }

        if (repo.contarCruces(req.laboratorioId(), req.fecha(),
                req.horaInicio(), req.horaFin()) > 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "El laboratorio ya está reservado en ese horario");
        }

        Reserva r = new Reserva();
        r.setUsuarioId(req.usuarioId());
        r.setLaboratorioId(req.laboratorioId());
        r.setFecha(req.fecha());
        r.setHoraInicio(req.horaInicio());
        r.setHoraFin(req.horaFin());
        r.setEstado("ACTIVA");
        return repo.save(r);
    }

    public List<Reserva> listar() { return repo.findAll(); }

    public List<Reserva> porUsuario(Long usuarioId) { return repo.findByUsuarioId(usuarioId); }

    public void eliminar(Long id) {
        if (!repo.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "La reserva " + id + " no existe");
        }
        repo.deleteById(id);
    }
}