package com.example.stin_news;

import config.SchedulingConfig;
import dtos.StockQueryDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import services.NewsEvaluationService;
import services.NewsScheduler;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class NewsSchedulerTest {

    @InjectMocks
    private NewsScheduler newsScheduler;

    @Mock
    private NewsEvaluationService newsEvaluationService;

    @Mock
    private SchedulingConfig stockConfig;

    /*@Test
    void evaluateAndStoreNewsEvery12Hours_invokesNewsEvaluationService() {
        // Nastavení mockovaného chování pro loadStockQueryDtos
        List<String> mockTickers = Arrays.asList("AAPL", "GOOGL"); // Opravte, aby vracelo List<String>
        when(stockConfig.getTickers()).thenReturn(mockTickers);

        List<StockQueryDto> mockStockQueryDtos = mockTickers.stream()
                .map(ticker -> new StockQueryDto(ticker, (int)System.currentTimeMillis()))
                .collect(Collectors.toList());
        when(newsScheduler.loadStockQueryDtos()).thenReturn(mockStockQueryDtos);

        // Nastavení mockovaného chování pro newsEvaluationService.evaluateAndStoreNews
        when(newsEvaluationService.evaluateAndStoreNews(mockStockQueryDtos)).thenReturn(Mono.empty());

        // Zavolání testované metody
        newsScheduler.evaluateAndStoreNewsEvery12Hours();

        // Ověření, že byla zavolána newsEvaluationService s očekávanými argumenty
        verify(newsEvaluationService, times(1)).evaluateAndStoreNews(mockStockQueryDtos);
    }*/

    @Test
    void loadStockQueryDtos_returnsListOfStockQueryDtosFromConfigTickers() {
        // Nastavení mockovaného chování pro stockConfig.getTickers()
        List<String> mockTickers = Arrays.asList("AAPL", "GOOGL", "MSFT");
        when(stockConfig.getTickers()).thenReturn(mockTickers);

        // Zavolání testované metody
        List<StockQueryDto> stockQueryDtos = newsScheduler.loadStockQueryDtos();

        // Ověření, že vrácený seznam není null a má správnou velikost
        assertNotNull(stockQueryDtos);
        assertEquals(mockTickers.size(), stockQueryDtos.size());

        // Ověření, že každý StockQueryDto má správné jméno (ticker)
        for (int i = 0; i < mockTickers.size(); i++) {
            assertEquals(mockTickers.get(i), stockQueryDtos.get(i).getName());
            assertNotNull(stockQueryDtos.get(i).getDate()); // Ověření, že datum je nastaveno
        }

        // Ověření, že metoda getTickers() byla zavolána právě jednou
        verify(stockConfig, times(1)).getTickers();
    }
}