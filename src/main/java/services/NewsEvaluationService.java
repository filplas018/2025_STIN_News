package services;

import clients.AlphaVantageClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dtos.StockQueryDto;
import lombok.RequiredArgsConstructor;
import models.StockSentiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import repositories.StockSentimentRepository;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NewsEvaluationService {

    private static final Logger logger = LoggerFactory.getLogger(NewsEvaluationService.class);
    private final AlphaVantageClient alphaVantageClient;



    @Autowired
    private SentimentAnalysisService sentimentAnalysisService;

    @Autowired
    private final StockSentimentRepository stockSentimentRepository;
    private final ObjectMapper objectMapper;

    @Value("${alpha_vantage.api.key}")
    private String apiKey;

    @Value("${sentiment.business-days-back}")
    private int BUSINESS_DAYS_BACK;

    @Value("${sentiment.news-min}")
    private int NEWS_MIN;

    @Value("${sentiment.news-max}")
    private int NEWS_MAX;



    /*     * Metoda pro vyhodnocení a uložení novinek pro akcie.
     *
     * @param stockQueryDtos Seznam dotazů na akcie.
     * @return Mono<Void> indikující dokončení operace.
     */
    public Mono<Void> evaluateAndStoreNews(List<StockQueryDto> stockQueryDtos) {
        DateTimeFormatter alphaVantageFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");
        ZoneId defaultZoneId = ZoneId.systemDefault();
        logger.debug("Received stockQueryDtos: {}", stockQueryDtos);

        return Flux.fromIterable(stockQueryDtos)
                .flatMap(stockQueryDto -> {
                    try {
                        logger.debug("Processing stockQueryDto: {}", stockQueryDto);
                        String ticker = stockQueryDto.getName();
                        long toDateInMillis = stockQueryDto.getDate();

                        LocalDate toLocalDate = Instant.ofEpochMilli(toDateInMillis)
                                .atZone(defaultZoneId)
                                .toLocalDate();

                        LocalDate fromLocalDate = subtractBusinessDays(toLocalDate, BUSINESS_DAYS_BACK);
                        LocalDateTime fromDateTime = fromLocalDate.atStartOfDay();
                        LocalDateTime toDateTime = toLocalDate.atTime(23, 59, 59);

                        // Používáme reaktivní přístup pro hledání existujícího sentimentu
                        return Mono.fromCallable(() ->
                                        stockSentimentRepository.findFirstByStockNameAndValidFromBetweenOrderByCreatedAtDesc(
                                                ticker, fromDateTime, toDateTime
                                        ))
                                .flatMap(existingSentiment -> {

                                    if (existingSentiment.isPresent()) {
                                        logger.info("Záznam pro {} a datum {} již existuje. Přeskakuji stahování a uložení.", ticker, fromDateTime);
                                        return Mono.empty(); // Záznam existuje, nic neděláme
                                    } else {
                                        // Záznam neexistuje, stahujeme data z Alpha Vantage
                                        String fromDateFormatted = fromDateTime.format(alphaVantageFormatter);
                                        String toDateFormatted = toDateTime.format(alphaVantageFormatter);
                                        String sort = "LATEST";
                                        int limit = NEWS_MAX;

                                        return alphaVantageClient.getCompanyNews(ticker, fromDateFormatted, toDateFormatted, sort, limit)
                                                .flatMap(response -> {
                                                    try {
                                                        JsonNode root = objectMapper.readTree(response);
                                                        JsonNode feedNode = root.path("feed");

                                                        int numberOfNews = feedNode.size();

                                                        if (numberOfNews < NEWS_MIN) {
                                                            logger.warn("Not enough news for {}: found {} news items, minimum required is {}.", ticker, numberOfNews, NEWS_MIN);
                                                            return Mono.empty(); // Neukládáme sentimenty
                                                        }

                                                        // Používáme metodu pro zpracování sentimentu a uložení
                                                        return sentimentAnalysisService.processSentimentAndSave(ticker, feedNode, fromDateTime);

                                                    } catch (IOException e) {
                                                        logger.error("Error parsing news for {}: {}", ticker, e.getMessage(), e);
                                                        return Mono.empty();
                                                    }
                                                })
                                                .onErrorResume(e -> {
                                                    logger.error("Error fetching news for {}: {}", ticker, e.getMessage(), e);
                                                    return Mono.empty();
                                                });
                                    }
                                });
                    } catch (Exception e) {
                        logger.error("Error processing stockQueryDto: {}", e.getMessage(), e);
                        return Mono.empty();
                    }
                })
                .then();
    }
    /**
     * Metoda pro získání ratingu pro dotazy na akcie.
     *
     * @param queries Seznam dotazů na akcie.
     * @return Mono obsahující seznam dotazů s nastaveným ratingem.
     */

    public Mono<List<StockQueryDto>> getRatingsForQueries(List<StockQueryDto> queries) {
        ZoneId zone = ZoneId.of("Europe/Prague");

        List<Mono<StockQueryDto>> resultMonos = queries.stream().map(query -> {
            LocalDate date = Instant.ofEpochMilli(query.getDate())
                    .atZone(zone)
                    .toLocalDate();

            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);

            return Mono.fromCallable(() -> {
                Optional<StockSentiment> sentimentOpt = stockSentimentRepository
                        .findFirstByStockNameAndValidFromBetweenOrderByCreatedAtDesc(query.getName(), start, end);

                // Pokud sentiment existuje, nastav rating
                if (sentimentOpt.isPresent()) {
                    query.setRating(sentimentOpt.get().getRating());
                } else {
                    // Pokud není žádný záznam, rating nech jako 0
                    query.setRating(0);
                }

                return query;
            });
        }).toList();

        return Flux.merge(resultMonos).collectList();
    }




/*     * Pomocná metoda pro odečtení pracovních dnů od zadaného data.
     *
     * @param date        Počáteční datum.
     * @param businessDays Počet pracovních dnů k odečtení.
     * @return Nové datum po odečtení pracovních dnů.
     */
    private LocalDate subtractBusinessDays(LocalDate date, int businessDays) {
        LocalDate result = date;
        int subtracted = 0;
        while (subtracted < businessDays) {
            result = result.minusDays(1);
            DayOfWeek day = result.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                subtracted++;
            }
        }
        return result;
    }

}