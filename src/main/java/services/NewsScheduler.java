package services;

import config.SchedulingConfig;
import dtos.StockQueryDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NewsScheduler {

    private final NewsEvaluationService newsEvaluationService;
    private final SchedulingConfig stockConfig; // Změna na final a injekce v konstruktoru

    // Konstruktor pro injektování NewsEvaluationService a SchedulingConfig
    public NewsScheduler(NewsEvaluationService newsEvaluationService, SchedulingConfig stockConfig) {
        this.newsEvaluationService = newsEvaluationService;
        this.stockConfig = stockConfig;
    }

    // Naplánovaná metoda, která se spustí každých 12 hodin
    @Scheduled(cron = "0 0 */12 * * *")
    public void evaluateAndStoreNewsEvery12Hours() {
        // Zde musíš načíst data pro `StockQueryDto`
        List<StockQueryDto> stockQueryDtos = loadStockQueryDtos();

        // Zavolání metody pro zpracování a uložení dat
        newsEvaluationService.evaluateAndStoreNews(stockQueryDtos)
                .subscribe(); // Spustí asynchronně
    }


    // Tuto metodu implementuj podle potřeby (např. načtení dat z databáze nebo jiného zdroje)
    public List<StockQueryDto> loadStockQueryDtos() {
        // Načtení seznamu tickerů z konfigurace
        List<String> tickers = stockConfig.getTickers();

        // Vytvoření seznamu StockQueryDto na základě tickerů
        return tickers.stream()
                .map(ticker -> {
                    // Vytváření nového StockQueryDto pro každý ticker
                    StockQueryDto dto = new StockQueryDto();
                    dto.setName(ticker);
                    dto.setDate(System.currentTimeMillis()); // Používáme aktuální čas
                    return dto;
                })
                .collect(Collectors.toList());
    }



}