package cr.ac.ucr.ms_reservas.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import cr.ac.ucr.ms_reservas.dto.LaboratorioDTO;

@FeignClient(name = "ms-laboratorios", fallback = LaboratorioClientFallback.class)
public interface LaboratorioClient {

    @GetMapping("/api/laboratorios/{id}")
    LaboratorioDTO obtener(@PathVariable("id") Long id);
}
