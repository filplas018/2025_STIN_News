package com.example.stin_news;

import clients.AlphaVantageClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.Mockito.*;
import dtos.StockQueryDto;
import models.StockSentiment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import repositories.StockSentimentRepository;
import services.NewsEvaluationService;
import services.SentimentAnalysisService;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NewsEvaluationServiceTest {

    @Mock
    private AlphaVantageClient alphaVantageClient;

    @Mock
    private SentimentAnalysisService sentimentAnalysisService;

    @Mock
    private StockSentimentRepository stockSentimentRepository;


    // @InjectMocks
    // private NewsEvaluationService newsScheduler;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NewsEvaluationService newsEvaluationService;

    @BeforeEach
    void setUp() {
        newsEvaluationService = new NewsEvaluationService(
                alphaVantageClient,
                stockSentimentRepository,
                objectMapper
        );

        // Injektuj autowirovaný service ručně
        ReflectionTestUtils.setField(newsEvaluationService, "sentimentAnalysisService", sentimentAnalysisService);

        // Injektuj hodnoty z @Value
        ReflectionTestUtils.setField(newsEvaluationService, "apiKey", "dummy-api-key");
        ReflectionTestUtils.setField(newsEvaluationService, "BUSINESS_DAYS_BACK", 3);
        ReflectionTestUtils.setField(newsEvaluationService, "NEWS_MIN", 2);
        ReflectionTestUtils.setField(newsEvaluationService, "NEWS_MAX", 10);
    }

    /*@Test
    void evaluateAndStoreNews_noExistingSentiment_fetchesAndSaves() throws IOException {
        // Arrange
        StockQueryDto stockQueryDto = new StockQueryDto("AAPL", (int)Instant.now().toEpochMilli());
        List<StockQueryDto> stockQueryDtos = Collections.singletonList(stockQueryDto);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        String mockResponse = "{\"feed\": [{\"ticker_sentiment\": [{\"ticker\": \"AAPL\", \"ticker_sentiment_score\": 0.7}]}]}";
        JsonNode mockRootNode = objectMapper.readTree(mockResponse); // **TATO INICIALIZACE JE KLÍČOVÁ**
        JsonNode mockFeedNode = mockRootNode.path("feed");
        StockSentiment savedSentiment = new StockSentiment();

        when(stockSentimentRepository.findFirstByStockNameAndValidFromBetweenOrderByCreatedAtDesc(eq("AAPL"), any(), any()))
                .thenReturn(Optional.empty());
        when(alphaVantageClient.getCompanyNews(eq("AAPL"), anyString(), anyString(), eq("LATEST"), anyInt()))
                .thenReturn(Mono.just(mockResponse));
        when(objectMapper.readTree(mockResponse)).thenReturn(mockRootNode); // Nyní vracíme správně inicializovaný mockRootNode
        when(sentimentAnalysisService.processSentimentAndSave(eq("AAPL"), eq(mockFeedNode), any()))
                .thenReturn(Mono.empty()); // Předpokládáme, že processSentimentAndSave() je void nebo vrací Mono<Void>

        // Act
        Mono<Void> result = newsEvaluationService.evaluateAndStoreNews(stockQueryDtos);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(alphaVantageClient, times(1)).getCompanyNews(eq("AAPL"), anyString(), anyString(), eq("LATEST"), anyInt());
        verify(sentimentAnalysisService, times(1)).processSentimentAndSave(eq("AAPL"), eq(mockFeedNode), any());
        verify(stockSentimentRepository, never()).save(any()); // Ukládání probíhá v SentimentAnalysisService
    }
*/
    @Test
    void evaluateAndStoreNews_existingSentiment_skipsFetchingAndSaving() {
        // Arrange
        StockQueryDto stockQueryDto = new StockQueryDto("MSFT", (int)Instant.now().toEpochMilli());
        List<StockQueryDto> stockQueryDtos = Collections.singletonList(stockQueryDto);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        StockSentiment existingSentiment = new StockSentiment();

        when(stockSentimentRepository.findFirstByStockNameAndValidFromBetweenOrderByCreatedAtDesc(eq("MSFT"), any(), any()))
                .thenReturn(Optional.of(existingSentiment));

        // Act
        Mono<Void> result = newsEvaluationService.evaluateAndStoreNews(stockQueryDtos);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(alphaVantageClient, never()).getCompanyNews(anyString(), anyString(), anyString(), anyString(), anyInt());
        verify(sentimentAnalysisService, never()).processSentimentAndSave(anyString(), any(), any());
        verify(stockSentimentRepository, never()).save(any());
    }

    /*@Test
    void evaluateAndStoreNews_alphaVantageClientThrowsException_logsErrorAndCompletes() {
        // Arrange
        StockQueryDto stockQueryDto = new StockQueryDto("GOOGL", (int)Instant.now().toEpochMilli());
        List<StockQueryDto> stockQueryDtos = Collections.singletonList(stockQueryDto);

        when(stockSentimentRepository.findFirstByStockNameAndValidFromBetweenOrderByCreatedAtDesc(eq("GOOGL"), any(), any()))
                .thenReturn(Optional.empty());
        when(alphaVantageClient.getCompanyNews(eq("GOOGL"), anyString(), anyString(), eq("LATEST"), anyInt()))
                .thenReturn(Mono.error(new IOException("API Error")));

        // Act
        Mono<Void> result = newsEvaluationService.evaluateAndStoreNews(stockQueryDtos);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(sentimentAnalysisService, never()).processSentimentAndSave(anyString(), any(), any());
        verify(stockSentimentRepository, never()).save(any());
    }*/

    // Další testy pro parsování JSONu s chybou, interní chyby atd.



    @Test
    void getRatingsForQueries_sentimentExists_setsRating() {
        // Arrange
        StockQueryDto stockQueryDto1 = new StockQueryDto("V", (int)Instant.now().toEpochMilli());
        StockQueryDto stockQueryDto2 = new StockQueryDto("MA", (int)Instant.now().minusSeconds(86400).toEpochMilli());
        List<StockQueryDto> queries = List.of(stockQueryDto1, stockQueryDto2);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startOfDay1 = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay1 = now.toLocalDate().atTime(23, 59, 59);
        LocalDateTime startOfDay2 = now.minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfDay2 = now.minusDays(1).toLocalDate().atTime(23, 59, 59);
        StockSentiment sentiment1 = new StockSentiment();
        sentiment1.setRating(7);
        StockSentiment sentiment2 = new StockSentiment();
        sentiment2.setRating(3);

        when(stockSentimentRepository.findFirstByStockNameAndValidFromBetweenOrderByCreatedAtDesc(eq("V"), any(), any()))
                .thenReturn(Optional.of(sentiment1));
        when(stockSentimentRepository.findFirstByStockNameAndValidFromBetweenOrderByCreatedAtDesc(eq("MA"), any(), any()))
                .thenReturn(Optional.of(sentiment2));

        // Act
        Mono<List<StockQueryDto>> result = newsEvaluationService.getRatingsForQueries(queries);

        // Assert
        StepVerifier.create(result)
                .assertNext(dtos -> {
                    assert dtos.size() == 2;
                    assert dtos.stream().anyMatch(dto -> dto.getName().equals("V") && dto.getRating() == 7);
                    assert dtos.stream().anyMatch(dto -> dto.getName().equals("MA") && dto.getRating() == 3);
                })
                .verifyComplete();
    }

    @Test
    void getRatingsForQueries_noSentimentExists_setsRatingToZero() {
        // Arrange
        StockQueryDto stockQueryDto = new StockQueryDto("PYPL", (int)Instant.now().toEpochMilli());
        List<StockQueryDto> queries = Collections.singletonList(stockQueryDto);

        when(stockSentimentRepository.findFirstByStockNameAndValidFromBetweenOrderByCreatedAtDesc(eq("PYPL"), any(), any()))
                .thenReturn(Optional.empty());

        // Act
        Mono<List<StockQueryDto>> result = newsEvaluationService.getRatingsForQueries(queries);

        // Assert
        StepVerifier.create(result)
                .assertNext(dtos -> {
                    assert dtos.size() == 1;
                    assert dtos.get(0).getName().equals("PYPL");
                    assert dtos.get(0).getRating() == 0;
                })
                .verifyComplete();
    }

    @Test
    void shouldDownloadAndStoreNewsIfNotAlreadyPresent() throws Exception {
        // Arrange
        String ticker = "AAPL";
        long dateMillis = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        StockQueryDto stockQuery = new StockQueryDto();
        stockQuery.setName(ticker);
        stockQuery.setDate(dateMillis);

        when(stockSentimentRepository.findFirstByStockNameAndValidFromBetweenOrderByCreatedAtDesc(
                anyString(), any(), any())
        ).thenReturn(Optional.empty());

        String mockResponse = """
                {
                  "feed": [
                    { "title": "News 1", "summary": "Something happened" },
                    { "title": "News 2", "summary": "Another thing" },
                    { "title": "News 3", "summary": "More info" }
                  ]
                }
                """;

        JsonNode feedNode = new ObjectMapper().readTree(mockResponse).get("feed");

        when(alphaVantageClient.getCompanyNews(anyString(), anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(Mono.just(mockResponse));

        when(objectMapper.readTree(mockResponse)).thenReturn(new ObjectMapper().readTree(mockResponse));

        when(sentimentAnalysisService.processSentimentAndSave(eq(ticker), eq(feedNode), any()))
                .thenReturn(Mono.empty()); // Simulujeme uložení sentimentu

        // Act
        Mono<Void> result = newsEvaluationService.evaluateAndStoreNews(List.of(stockQuery));

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        // Verifikace
        verify(alphaVantageClient, times(1)).getCompanyNews(anyString(), anyString(), anyString(), anyString(), anyInt());
        verify(sentimentAnalysisService, times(1)).processSentimentAndSave(eq(ticker), eq(feedNode), any());
    }

    // Další testy pro okrajové případy a chyby v getRatingsForQueries
}