package cr.ac.ucr.ms_reservas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "reservas")
public class ReservasProperties {
    private int maxHoras = 2;

    public int getMaxHoras() { return maxHoras; }
    public void setMaxHoras(int maxHoras) { this.maxHoras = maxHoras; }
}