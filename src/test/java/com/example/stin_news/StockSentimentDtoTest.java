package com.example.stin_news;



import dtos.StockSentimentDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class StockSentimentDtoTest {

    private static Validator v;

    @BeforeAll
    static void setUpV() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        v = vf.getValidator();
    }

    @Test
    void testValidDto() {
        StockSentimentDto dto = new StockSentimentDto();
        dto.setStockName("TSLA");
        dto.setRating(5);
        dto.setValidFrom(LocalDateTime.now().plusHours(1));
        dto.setArticleText("Pozitivní zprávy o výnosech TSLA.");
        Set<ConstraintViolation<StockSentimentDto>> violations = v.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNotBlankStockName() {
        StockSentimentDto dto = new StockSentimentDto();
        dto.setStockName("");
        dto.setRating(3);
        dto.setValidFrom(LocalDateTime.now().plusHours(1));
        dto.setArticleText("Neutrální článek.");
        Set<ConstraintViolation<StockSentimentDto>> violations = v.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("must not be blank", violations.iterator().next().getMessage());
    }

    @Test
    void testMinRating() {
        StockSentimentDto dto = new StockSentimentDto();
        dto.setStockName("AAPL");
        dto.setRating(-11);
        dto.setValidFrom(LocalDateTime.now().plusDays(1));
        dto.setArticleText("Negativní výhled pro AAPL.");
        Set<ConstraintViolation<StockSentimentDto>> violations = v.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("must be greater than or equal to -10", violations.iterator().next().getMessage());
    }

    @Test
    void testMaxRating() {
        StockSentimentDto dto = new StockSentimentDto();
        dto.setStockName("GOOG");
        dto.setRating(11);
        dto.setValidFrom(LocalDateTime.now().plusMinutes(30));
        dto.setArticleText("Velmi pozitivní sentiment.");
        Set<ConstraintViolation<StockSentimentDto>> violations = v.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("must be less than or equal to 10", violations.iterator().next().getMessage());
    }

    @Test
    void testNotNullValidFrom() {
        StockSentimentDto dto = new StockSentimentDto();
        dto.setStockName("AMZN");
        dto.setRating(0);
        dto.setValidFrom(null);
        dto.setArticleText("Analýza trhu.");
        Set<ConstraintViolation<StockSentimentDto>> violations = v.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testGettersSetters() {
        StockSentimentDto dto = new StockSentimentDto();
        String sn = "MSFT";
        int r = -5;
        LocalDateTime vf = LocalDateTime.now().plusDays(3);
        String at = "Smíšené signály pro akcie MSFT.";
        dto.setStockName(sn);
        dto.setRating(r);
        dto.setValidFrom(vf);
        dto.setArticleText(at);
        assertEquals(sn, dto.getStockName());
        assertEquals(r, dto.getRating());
        assertEquals(vf, dto.getValidFrom());
        assertEquals(at, dto.getArticleText());
    }

    @Test
    void testArticleTextNull() {
        StockSentimentDto dto = new StockSentimentDto();
        dto.setStockName("META");
        dto.setRating(8);
        dto.setValidFrom(LocalDateTime.now().plusHours(2));
        dto.setArticleText(null);
        Set<ConstraintViolation<StockSentimentDto>> violations = v.validate(dto);
        assertTrue(violations.isEmpty());
    }
}