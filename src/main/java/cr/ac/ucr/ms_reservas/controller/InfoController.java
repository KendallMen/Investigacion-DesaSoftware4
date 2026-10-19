package cr.ac.ucr.ms_reservas.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {
    @GetMapping("/api/reservas/info")
    public Map<String, String> info() throws UnknownHostException {
        return Map.of("pod", InetAddress.getLocalHost().getHostName());
    }
}