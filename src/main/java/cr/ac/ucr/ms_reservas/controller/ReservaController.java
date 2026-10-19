package cr.ac.ucr.ms_reservas.controller;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import cr.ac.ucr.ms_reservas.dto.ReservaRequest;
import cr.ac.ucr.ms_reservas.model.Reserva;
import cr.ac.ucr.ms_reservas.service.ReservaService;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reserva crear(@Valid @RequestBody ReservaRequest req) { return service.crear(req); }

    @GetMapping
    public List<Reserva> listar() { return service.listar(); }

    @GetMapping("/usuario/{id}")
    public List<Reserva> porUsuario(@PathVariable Long id) { return service.porUsuario(id); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { service.eliminar(id); }
}