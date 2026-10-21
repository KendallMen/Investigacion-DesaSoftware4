package cr.ac.ucr.ms_usuarios.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    @GetMapping("/api/usuarios/info")
    public Map<String, String> info() throws UnknownHostException {
        return Map.of("servicio", "ms-usuarios",
                      "pod", InetAddress.getLocalHost().getHostName());
    }
}
