package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.StockSentimentDto;
import models.StockSentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import repositories.StockSentimentRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SentimentAnalysisService {
    @Value("${sentiment.business-days-back}")
    private int BUSINESS_DAYS_BACK;

    @Value("${sentiment.news-min}")
    private int NEWS_MIN;

    @Value("${sentiment.news-max}")
    private int NEWS_MAX;

    @Autowired
    private StockSentimentRepository stockSentimentRepository;

    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalysisService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();


/*    * Metoda pro výpočet průměrného sentimentu pro daný ticker.
     *
     * @param feedNode JSON uzel s novinkami.
     * @param ticker   Název akcie.
     * @return Průměrný sentiment pro daný ticker.
     * @throws IOException Pokud dojde k chybě při zpracování JSON.
     */
    public double calculateAverageSentiment(JsonNode feedNode, String ticker) throws IOException {
        List<Double> tickerScores = new ArrayList<>();

        if (feedNode.isArray()) {
            for (JsonNode articleNode : feedNode) {
                JsonNode tickerSentimentArray = articleNode.path("ticker_sentiment");
                if (tickerSentimentArray.isArray()) {
                    for (JsonNode tickerSentiment : tickerSentimentArray) {
                        String tickerInArticle = tickerSentiment.path("ticker").asText();
                        if (ticker.equalsIgnoreCase(tickerInArticle)) {
                            double score = tickerSentiment.path("ticker_sentiment_score").asDouble(0.0);
                            tickerScores.add(score);
                        }
                    }
                }
            }
        }

        if (tickerScores.isEmpty()) {
            return Double.NaN; // Pokud není žádný sentiment, vrátíme NaN
        }

        return tickerScores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * Metoda pro zpracování sentimentu a jeho uložení do databáze.
     *
     * @param ticker       Název akcie.
     * @param feedNode     JSON uzel s novinkami.
     * @param fromDateTime Časový údaj pro validaci.
     * @return Mono<Void> indikující dokončení operace.
     */
    public Mono<Void> processSentimentAndSave(String ticker, JsonNode feedNode, LocalDateTime fromDateTime) {
        try {
            // Používáme SentimentAnalysisService pro výpočet průměrného sentimentu
            double averageSentiment = calculateAverageSentiment(feedNode, ticker);

            if (Double.isNaN(averageSentiment)) {
                logger.warn("No sentiment scores found for {} in the last {} days.", ticker, BUSINESS_DAYS_BACK);
                return Mono.empty();
            }



            int finalRating = (int) Math.round(averageSentiment * 10);

            StockSentiment newSentiment = new StockSentiment();
            newSentiment.setStockName(ticker);
            newSentiment.setRating(finalRating);
            newSentiment.setValidFrom(fromDateTime);
            newSentiment.setCreatedAt(LocalDateTime.now());

            return Mono.fromRunnable(() -> stockSentimentRepository.save(newSentiment))
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();

        } catch (IOException e) {
            logger.error("Error parsing news for {}: {}", ticker, e.getMessage(), e);
            return Mono.empty();
        }
    }


}