package com.example.stin_news;



import dtos.StockQueryDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StockQueryDtoTest {

    @Test
    void testNoArgsConstructor() {
        StockQueryDto dto = new StockQueryDto();
        assertNull(dto.getName());
        assertEquals(0L, dto.getDate());
        assertNull(dto.getRating());
        assertNull(dto.getSell());
    }

    @Test
    void testConstructorWithNameAndRating() {
        StockQueryDto dto = new StockQueryDto("Apple", 5);
        assertEquals("Apple", dto.getName());
        assertEquals(5, dto.getRating());
        assertEquals(0L, dto.getDate()); // Default value
        assertNull(dto.getSell());      // Default value
    }

    @Test
    void testSettersAndGetters() {
        StockQueryDto dto = new StockQueryDto();
        dto.setName("Google");
        dto.setDate(1678886400000L); // Příklad timestampu
        dto.setRating(3);
        dto.setSell(1);

        assertEquals("Google", dto.getName());
        assertEquals(1678886400000L, dto.getDate());
        assertEquals(3, dto.getRating());
        assertEquals(1, dto.getSell());
    }

    @Test
    void testSetName() {
        StockQueryDto dto = new StockQueryDto();
        dto.setName("Microsoft");
        assertEquals("Microsoft", dto.getName());
    }

    @Test
    void testSetDate() {
        StockQueryDto dto = new StockQueryDto();
        long testDate = System.currentTimeMillis();
        dto.setDate(testDate);
        assertEquals(testDate, dto.getDate());
    }

    @Test
    void testSetRating() {
        StockQueryDto dto = new StockQueryDto();
        dto.setRating(-1);
        assertEquals(-1, dto.getRating());
    }

    @Test
    void testSetSell() {
        StockQueryDto dto = new StockQueryDto();
        dto.setSell(0);
        assertEquals(0, dto.getSell());
    }

    @Test
    void testAllGetters() {
        StockQueryDto dto = new StockQueryDto("Amazon", 4);
        dto.setDate(1681564800000L);
        dto.setSell(null); // Může být null

        assertEquals("Amazon", dto.getName());
        assertEquals(1681564800000L, dto.getDate());
        assertEquals(4, dto.getRating());
        assertNull(dto.getSell());
    }
}