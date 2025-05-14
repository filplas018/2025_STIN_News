package controllers;

import dtos.StockQueryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;
import repositories.StockSentimentRepository;
import services.NewsEvaluationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sentiment")
public class SentimentController {

    private static final Logger logger = LoggerFactory.getLogger(SentimentController.class);
    private final NewsEvaluationService newsEvaluationService;

    private final StockSentimentRepository stockSentimentRepository;

    @Value("${alpha_vantage.api.key}")
    private String apiKey;

    public SentimentController(NewsEvaluationService newsEvaluationService, StockSentimentRepository stockSentimentRepository) {
        this.newsEvaluationService = newsEvaluationService;
        this.stockSentimentRepository = stockSentimentRepository;
    }

    // Jednoduchý POST endpoint, který přijme seznam tickerů
    /*@PostMapping("/evaluate")
    public Mono<Void> evaluateSentiment(@RequestBody List<String> tickers) {
        return newsEvaluationService.evaluateAndStoreNews(tickers);
    }*/

    @PostMapping("/evaluate")
    public Mono<Void> evaluateSentiment(@RequestBody List<StockQueryDto> stockQueryDtos) {
        return newsEvaluationService.evaluateAndStoreNews(stockQueryDtos); // Předáváme seznam DTO
    }
    @PostMapping("/ratings")
    public Mono<List<StockQueryDto>> getRatings(@RequestBody List<StockQueryDto> queries) {
        List<StockQueryDto> validQueries = queries.stream()
                .filter(query -> query != null) // Odfiltrujeme neúspěšně zdeserializované (null) prvky
                .collect(Collectors.toList());

        logger.info("Počet validních dotazů k vyhodnocení: {}", validQueries.size());

        return newsEvaluationService.getRatingsForQueries(validQueries);
    }

}