package cr.ac.ucr.ms_reservas.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import cr.ac.ucr.ms_reservas.excepcion.ApiException;
import feign.FeignException;

@Component
public class UsuarioClientFallbackFactory implements FallbackFactory<UsuarioClient> {

    @Override
    public UsuarioClient create(Throwable causa) {
        return id -> {
            if (causa instanceof FeignException.NotFound) {
                throw new ApiException(HttpStatus.NOT_FOUND, "El usuario " + id + " no existe");
            }
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Servicio de usuarios no disponible, intente más tarde");
        };
    }
}