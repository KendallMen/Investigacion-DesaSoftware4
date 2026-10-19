package cr.ac.ucr.ms_reservas.controller;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Profile("mock")
@RestController
public class MockController {

    @GetMapping("/api/usuarios/{id}")
    public Map<String, Object> usuario(@PathVariable Long id) {
        return Map.of("id", id, "nombre", "Usuario " + id, "carnet", "C0000" + id,
                      "correo", "u" + id + "@ucr.ac.cr", "carrera", "Informática");
    }

    @GetMapping("/api/laboratorios/{id}")
    public Map<String, Object> laboratorio(@PathVariable Long id) {
        return Map.of("id", id, "nombre", "Lab " + id, "edificio", "Edificio A",
                      "capacidad", 30, "disponible", id != 4);
    }
}