package com.example.stin_news;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.StockSentimentDto;
import models.StockSentiment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.test.StepVerifier;
import repositories.StockSentimentRepository;
import services.SentimentAnalysisService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Povolujeme lenientní mockování pro nerealizované interakce
class SentimentAnalysisServiceTest {

    @InjectMocks
    private SentimentAnalysisService sentimentAnalysisService;

    @Mock
    private StockSentimentRepository stockSentimentRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void calculateAverageSentiment_withScores_returnsAverage() throws IOException {
        String ticker = "AAPL";
        String jsonResponse = "{\"feed\": [" +
                "{\"ticker_sentiment\": [{\"ticker\": \"AAPL\", \"ticker_sentiment_score\": 0.5}]}, " +
                "{\"ticker_sentiment\": [{\"ticker\": \"MSFT\", \"ticker_sentiment_score\": 0.2}, {\"ticker\": \"AAPL\", \"ticker_sentiment_score\": 0.7}]}" +
                "]}";
        JsonNode feedNode = objectMapper.readTree(jsonResponse).path("feed");

        double averageSentiment = sentimentAnalysisService.calculateAverageSentiment(feedNode, ticker);

        assertEquals(0.6, averageSentiment, 0.001);
    }

    @Test
    void calculateAverageSentiment_noScoresForTicker_returnsNaN() throws IOException {
        String ticker = "AAPL";
        String jsonResponse = "{\"feed\": [" +
                "{\"ticker_sentiment\": [{\"ticker\": \"MSFT\", \"ticker_sentiment_score\": 0.5}]}" +
                "]}";
        JsonNode feedNode = objectMapper.readTree(jsonResponse).path("feed");

        double averageSentiment = sentimentAnalysisService.calculateAverageSentiment(feedNode, ticker);

        assertTrue(Double.isNaN(averageSentiment));
    }

    @Test
    void calculateAverageSentiment_emptyFeed_returnsNaN() throws IOException {
        String ticker = "AAPL";
        String jsonResponse = "{\"feed\": []}";
        JsonNode feedNode = objectMapper.readTree(jsonResponse).path("feed");

        double averageSentiment = sentimentAnalysisService.calculateAverageSentiment(feedNode, ticker);

        assertTrue(Double.isNaN(averageSentiment));
    }

   /* @Test
    void processSentimentAndSave_validSentiment_savesToRepository() throws IOException {
        String ticker = "AAPL";
        LocalDateTime now = LocalDateTime.now();
        String jsonResponse = "{\"feed\": [" +
                "{\"ticker_sentiment\": [{\"ticker\": \"AAPL\", \"ticker_sentiment_score\": 0.6}]}" +
                "]}";
        JsonNode feedNode = objectMapper.readTree(jsonResponse).path("feed");

        // Nastavení konfiguračních hodnot pro test - ZKONTROLUJTE TYPY!
        ReflectionTestUtils.setField(sentimentAnalysisService, "BUSINESS_DAYS_BACK", 7); // Předpokládám, že BUSINESS_DAYS_BACK je int
        ReflectionTestUtils.setField(sentimentAnalysisService, "NEWS_MIN", -1);      // Nastavte celé číslo pro int NEWS_MIN (pokud je int)
        ReflectionTestUtils.setField(sentimentAnalysisService, "NEWS_MAX", 1);      // Předpokládám, že NEWS_MAX je int nebo double

        doNothing().when(stockSentimentRepository).save(any(StockSentiment.class()));

        Mono<Void> result = sentimentAnalysisService.processSentimentAndSave(ticker, feedNode, now);

        StepVerifier.create(result)
                .verifyComplete();

        verify(stockSentimentRepository, times(1)).save(argThat(sentiment ->
                sentiment.getStockName().equals(ticker) &&
                        sentiment.getRating() == 6 &&
                        sentiment.getValidFrom().equals(now)
        ));
    }*/

    @Test
    void processSentimentAndSave_noSentiment_doesNotSave() throws IOException {
        String ticker = "AAPL";
        LocalDateTime now = LocalDateTime.now();
        String jsonResponse = "{\"feed\": []}";
        JsonNode feedNode = objectMapper.readTree(jsonResponse).path("feed");

        // Nastavení konfiguračních hodnot pro test - Ujistěte se, že typy odpovídají!
        ReflectionTestUtils.setField(sentimentAnalysisService, "BUSINESS_DAYS_BACK", 7); // Předpokládám, že BUSINESS_DAYS_BACK je int
        ReflectionTestUtils.setField(sentimentAnalysisService, "NEWS_MIN", -1);      // Nastavte celé číslo pro int NEWS_MIN
        ReflectionTestUtils.setField(sentimentAnalysisService, "NEWS_MAX", 1);      // Předpokládám, že NEWS_MAX je int nebo double

        Mono<Void> result = sentimentAnalysisService.processSentimentAndSave(ticker, feedNode, now);

        StepVerifier.create(result)
                .verifyComplete();

        verify(stockSentimentRepository, never()).save(any());
    }


    /*@Test
    void processSentimentAndSave_ioException_completesWithoutSaving() throws IOException {
        String ticker = "AAPL";
        LocalDateTime now = LocalDateTime.now();
        JsonNode feedNode = mock(JsonNode.class);

        // Nastavení konfiguračních hodnot pro test
        ReflectionTestUtils.setField(sentimentAnalysisService, "BUSINESS_DAYS_BACK", 7);
        ReflectionTestUtils.setField(sentimentAnalysisService, "NEWS_MIN", -1);
        ReflectionTestUtils.setField(sentimentAnalysisService, "NEWS_MAX", 1);

        // Předpokládám, že calculateAverageSentiment interně volá isArray() na feedNode
        // a my chceme simulovat IOException při tomto volání.
        when(feedNode.isArray()).thenThrow(new IOException("Simulated IO Exception"));

        // Voláme metodu, kterou testujeme
        Mono<Void> result = sentimentAnalysisService.processSentimentAndSave(ticker, feedNode, now);

        StepVerifier.create(result)
                .verifyComplete();

        // Ověřujeme, že save nebyl volán
        verify(stockSentimentRepository, never()).save(any());
        // Ověřujeme, že calculateAverageSentiment byl volán (případně s mockem feedNode)
        verify(sentimentAnalysisService, times(1)).calculateAverageSentiment(same(feedNode), eq(ticker));
    }*/

    /*@Test
    void processSentimentAndSave_notEnoughNews_doesNotSave() throws IOException {
        String ticker = "AAPL";
        LocalDateTime now = LocalDateTime.now();
        String jsonResponse = "{\"feed\": [" +
                "{\"ticker_sentiment\": [{\"ticker\": \"AAPL\", \"ticker_sentiment_score\": 0.6}]}" +
                "]}";
        JsonNode feedNode = objectMapper.readTree(jsonResponse).path("feed");

        // Nastavíme NEWS_MIN na vyšší hodnotu pro účely tohoto testu
        ReflectionTestUtils.setField(sentimentAnalysisService, "NEWS_MIN", 2);

        Mono<Void> result = sentimentAnalysisService.processSentimentAndSave(ticker, feedNode, now);

        StepVerifier.create(result)
                .verifyComplete();

        verify(stockSentimentRepository, never()).save(any());

        // Nezapomeňte, že tímto přepíšete skutečnou konfiguraci pro tento test
    }*/
}