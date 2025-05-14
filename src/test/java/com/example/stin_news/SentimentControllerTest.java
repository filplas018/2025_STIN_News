package com.example.stin_news;

import controllers.SentimentController;
import dtos.StockQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;
import repositories.StockSentimentRepository;
import services.NewsEvaluationService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SentimentControllerTest {

    @InjectMocks
    private SentimentController sentimentController;

    @Mock
    private NewsEvaluationService newsEvaluationService;

    @Mock
    private StockSentimentRepository stockSentimentRepository;

    @Value("${alpha_vantage.api.key}")
    private String apiKey;

    @Test
    void evaluateSentiment_validTickers_callsNewsEvaluationService() {
        List<StockQueryDto> tickers = Arrays.asList(
                new StockQueryDto("AAPL", 0),
                new StockQueryDto("GOOGL", 0)
        );

        when(newsEvaluationService.evaluateAndStoreNews(tickers)).thenReturn(Mono.empty());

        sentimentController.evaluateSentiment(tickers).block(); // Block for test synchronization

        verify(newsEvaluationService).evaluateAndStoreNews(tickers);
    }

    @Test
    void getRatings_validQueries_returnsRatingsFromNewsEvaluationService() {
        List<StockQueryDto> queries = Arrays.asList(
                new StockQueryDto("AAPL", 0),
                new StockQueryDto("MSFT", 0)
        );
        List<StockQueryDto> expectedRatings = Arrays.asList(
                new StockQueryDto("AAPL", -5),
                new StockQueryDto("MSFT", 0)
        );

        when(newsEvaluationService.getRatingsForQueries(queries)).thenReturn(Mono.just(expectedRatings));

        List<StockQueryDto> actualRatings = sentimentController.getRatings(queries).block();

        verify(newsEvaluationService).getRatingsForQueries(queries);
        assertEquals(expectedRatings.size(), actualRatings.size());
        assertEquals(expectedRatings.get(0).getName(), actualRatings.get(0).getName());
        assertEquals(expectedRatings.get(0).getRating(), actualRatings.get(0).getRating());
        assertEquals(expectedRatings.get(1).getName(), actualRatings.get(1).getName());
        assertEquals(expectedRatings.get(1).getRating(), actualRatings.get(1).getRating());
    }
}