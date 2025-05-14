package config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Konfigurace plánování

    @Value("#{'${stock.tickers}'.split(',')}")
    private List<String> tickers;

    public List<String> getTickers() {
        return tickers;
    }
}