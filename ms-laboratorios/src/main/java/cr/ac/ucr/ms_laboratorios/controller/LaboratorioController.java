package cr.ac.ucr.ms_laboratorios.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import cr.ac.ucr.ms_laboratorios.model.Laboratorio;
import cr.ac.ucr.ms_laboratorios.service.LaboratorioService;

@RestController
@RequestMapping("/api/laboratorios")
public class LaboratorioController {

    private final LaboratorioService service;

    public LaboratorioController(LaboratorioService service) { this.service = service; }

    @GetMapping
    public List<Laboratorio> listar() { return service.listar(); }

    @GetMapping("/{id}")
    public Laboratorio obtener(@PathVariable Long id) { return service.obtener(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Laboratorio crear(@Valid @RequestBody Laboratorio laboratorio) { return service.crear(laboratorio); }

    @PutMapping("/{id}")
    public Laboratorio actualizar(@PathVariable Long id, @Valid @RequestBody Laboratorio laboratorio) {
        return service.actualizar(id, laboratorio);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}
