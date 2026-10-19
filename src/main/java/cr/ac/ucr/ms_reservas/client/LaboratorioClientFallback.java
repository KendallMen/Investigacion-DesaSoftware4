package cr.ac.ucr.ms_reservas.client;

import org.springframework.stereotype.Component;

import cr.ac.ucr.ms_reservas.dto.LaboratorioDTO;

@Component
public class LaboratorioClientFallback implements LaboratorioClient {

    @Override
    public LaboratorioDTO obtener(Long id) {
        throw new IllegalStateException(
            "Servicio de laboratorios no disponible; intente más tarde");
    }
}
