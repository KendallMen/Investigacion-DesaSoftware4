package cr.ac.ucr.ms_reservas.client;

import cr.ac.ucr.ms_reservas.dto.UsuarioDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import cr.ac.ucr.ms_reservas.client.UsuarioClientFallbackFactory;

@FeignClient(name = "ms-usuarios",
             url = "${clientes.usuarios.url:}",
             fallbackFactory = UsuarioClientFallbackFactory.class)
public interface UsuarioClient {

    @GetMapping("/api/usuarios/{id}")
    UsuarioDTO obtener(@PathVariable("id") Long id);
}