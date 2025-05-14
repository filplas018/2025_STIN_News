package repositories;



import models.StockSentiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockSentimentRepository extends JpaRepository<StockSentiment, Long> {
    List<StockSentiment> findByStockNameAndValidFromBetween(String stockName, LocalDateTime startDate, LocalDateTime endDate);
    Optional<StockSentiment> findFirstByStockNameAndValidFromBetweenOrderByCreatedAtDesc(
            String stockName, LocalDateTime from, LocalDateTime to);


}