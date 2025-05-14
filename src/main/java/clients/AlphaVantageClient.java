package clients;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
@Service
@RequiredArgsConstructor
public class AlphaVantageClient {

    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageClient.class);

    private final WebClient webClient;

    @Value("${alpha_vantage.api.key}")
    private String API_KEY;

    @Value("${sentiment.attempts}")
    private int ATTEMPTS;

    @Value("${sentiment.attempts-delay}")
    private int ATTEMPTS_DELAY;


    public Mono<String> getCompanyNews(String ticker, String fromDateTime, String toDateTime, String sort, int limit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/query")
                        .queryParam("function", "NEWS_SENTIMENT")
                        .queryParam("tickers", ticker)
                        .queryParam("time_from", fromDateTime)
                        .queryParam("time_to", toDateTime)
                        .queryParam("sort", sort)
                        .queryParam("limit", limit)
                        .queryParam("apikey", API_KEY)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(
                        Retry.fixedDelay(ATTEMPTS, Duration.ofSeconds(ATTEMPTS_DELAY))
                                .filter(throwable -> {
                                    logger.warn("Retrying after error: {}", throwable.getMessage());
                                    return true;
                                })
                );
    }
}
