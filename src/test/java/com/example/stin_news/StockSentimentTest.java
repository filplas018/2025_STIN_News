package com.example.stin_news;


import models.StockSentiment;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class StockSentimentTest {

    @Test
    void testGettersAndSetters() {
        StockSentiment sentiment = new StockSentiment();
        Long id = 1L;
        String stockName = "Tesla";
        int rating = 5;
        LocalDateTime validFrom = LocalDateTime.now().plusHours(1);
        LocalDateTime createdAt = LocalDateTime.now();

        sentiment.setId(id);
        sentiment.setStockName(stockName);
        sentiment.setRating(rating);
        sentiment.setValidFrom(validFrom);
        sentiment.setCreatedAt(createdAt);

        assertEquals(id, sentiment.getId());
        assertEquals(stockName, sentiment.getStockName());
        assertEquals(rating, sentiment.getRating());
        assertEquals(validFrom, sentiment.getValidFrom());
        assertEquals(createdAt, sentiment.getCreatedAt());
    }

    @Test
    void testPrePersist() throws InterruptedException {
        StockSentiment sentiment = new StockSentiment();
        LocalDateTime beforeCreation = LocalDateTime.now();
        // Simulate a short delay to ensure createdAt is after beforeCreation
        Thread.sleep(10);
        sentiment.onCreate();
        LocalDateTime afterCreation = LocalDateTime.now();

        assertNotNull(sentiment.getCreatedAt());
        assertTrue(sentiment.getCreatedAt().isAfter(beforeCreation) || sentiment.getCreatedAt().isEqual(beforeCreation));
        assertTrue(sentiment.getCreatedAt().isBefore(afterCreation) || sentiment.getCreatedAt().isEqual(afterCreation));
    }

    @Test
    void testDefaultValues() {
        StockSentiment sentiment = new StockSentiment();
        assertNull(sentiment.getId());
        assertNull(sentiment.getStockName());
        assertEquals(0, sentiment.getRating()); // Default value for int is 0
        assertNull(sentiment.getValidFrom());
        assertNull(sentiment.getCreatedAt()); // onCreate is only called on persist
    }

    @Test
    void testSetAndGetId() {
        StockSentiment sentiment = new StockSentiment();
        Long id = 10L;
        sentiment.setId(id);
        assertEquals(id, sentiment.getId());
    }

    @Test
    void testSetAndGetStockName() {
        StockSentiment sentiment = new StockSentiment();
        String name = "Amazon";
        sentiment.setStockName(name);
        assertEquals(name, sentiment.getStockName());
    }

    @Test
    void testSetAndGetRating() {
        StockSentiment sentiment = new StockSentiment();
        int ratingValue = -3;
        sentiment.setRating(ratingValue);
        assertEquals(ratingValue, sentiment.getRating());
    }

    @Test
    void testSetAndGetValidFrom() {
        StockSentiment sentiment = new StockSentiment();
        LocalDateTime validTime = LocalDateTime.now().plusDays(7);
        sentiment.setValidFrom(validTime);
        assertEquals(validTime, sentiment.getValidFrom());
    }

    @Test
    void testSetAndGetCreatedAt() {
        StockSentiment sentiment = new StockSentiment();
        LocalDateTime createdTime = LocalDateTime.now().minusDays(1);
        sentiment.setCreatedAt(createdTime);
        assertEquals(createdTime, sentiment.getCreatedAt());
    }
}