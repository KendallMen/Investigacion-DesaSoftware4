package cr.ac.ucr.ms_laboratorios.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cr.ac.ucr.ms_laboratorios.excepcion.ApiException;
import cr.ac.ucr.ms_laboratorios.model.Laboratorio;
import cr.ac.ucr.ms_laboratorios.repository.LaboratorioRepository;

@Service
public class LaboratorioService {

    private final LaboratorioRepository repository;

    public LaboratorioService(LaboratorioRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<Laboratorio> listar() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Laboratorio obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "El laboratorio " + id + " no existe"));
    }

    @Transactional
    public Laboratorio crear(Laboratorio laboratorio) {
        if (repository.existsByNombre(laboratorio.getNombre())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un laboratorio llamado " + laboratorio.getNombre());
        }
        laboratorio.setId(null); // el id lo genera la base
        return repository.save(laboratorio);
    }

    @Transactional
    public Laboratorio actualizar(Long id, Laboratorio datos) {
        Laboratorio actual = obtener(id);
        if (repository.existsByNombreAndIdNot(datos.getNombre(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un laboratorio llamado " + datos.getNombre());
        }
        actual.setNombre(datos.getNombre());
        actual.setEdificio(datos.getEdificio());
        actual.setCapacidad(datos.getCapacidad());
        actual.setDisponible(datos.getDisponible());
        return repository.save(actual);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.delete(obtener(id));
    }
}
