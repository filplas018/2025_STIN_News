package com.example.stin_news;


import models.ApplicationUser;
import models.Stock;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StockTest {

    @Test
    void testGettersAndSetters() {
        Stock stock = new Stock();
        stock.setId(1L);
        stock.setName("Apple");
        stock.setSold(true);
        stock.setFavourite(false);
        ApplicationUser user = new ApplicationUser();
        stock.setUser(user);

        assertEquals(1L, stock.getId());
        assertEquals("Apple", stock.getName());
        assertTrue(stock.isSold());
        assertFalse(stock.isFavourite());
        assertEquals(user, stock.getUser());
    }

    @Test
    void testNoArgsConstructor() {
        Stock stock = new Stock();
        assertNull(stock.getId());
        assertNull(stock.getName());
        assertFalse(stock.isSold()); // Default value for boolean is false
        assertFalse(stock.isFavourite()); // Default value for boolean is false
        assertNull(stock.getUser());
    }

    @Test
    void testConstructorWithNameIsSoldAndUser() {
        ApplicationUser user = new ApplicationUser();
        Stock stock = new Stock("Google", false, user);

        assertNull(stock.getId()); // ID is generated
        assertEquals("Google", stock.getName());
        assertFalse(stock.isSold());
        assertFalse(stock.isFavourite()); // Default value
        assertEquals(user, stock.getUser());
    }

    @Test
    void testSetAndGetId() {
        Stock stock = new Stock();
        stock.setId(2L);
        assertEquals(2L, stock.getId());
    }

    @Test
    void testSetAndGetName() {
        Stock stock = new Stock();
        stock.setName("Microsoft");
        assertEquals("Microsoft", stock.getName());
    }

    @Test
    void testSetAndIsSold() {
        Stock stock = new Stock();
        stock.setSold(true);
        assertTrue(stock.isSold());
        stock.setSold(false);
        assertFalse(stock.isSold());
    }

    @Test
    void testSetAndIsFavourite() {
        Stock stock = new Stock();
        stock.setFavourite(true);
        assertTrue(stock.isFavourite());
        stock.setFavourite(false);
        assertFalse(stock.isFavourite());
    }

    @Test
    void testSetAndGetUser() {
        Stock stock = new Stock();
        ApplicationUser user1 = new ApplicationUser();
        ApplicationUser user2 = new ApplicationUser();
        stock.setUser(user1);
        assertEquals(user1, stock.getUser());
        stock.setUser(user2);
        assertEquals(user2, stock.getUser());
    }
}