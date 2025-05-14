package com.example.stin_news;
import controllers.StockController;
import models.ApplicationUser;
import models.Stock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import repositories.StockRepository;
import services.UserService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class StockControllerTest {

    @Mock private UserService userService;
    @Mock private StockRepository stockRepository;
    @Mock private Model model;

    @InjectMocks private StockController stockController;

    private final String email = "test@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockSecurityContext();
    }

    private void mockSecurityContext() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void showStocks_shouldAddStocksToModelAndReturnView() {
        List<Stock> stockList = List.of(new Stock("AAPL", false, null));
        when(userService.getUserStocks(email)).thenReturn(stockList);

        String view = stockController.showStocks(model);

        verify(model).addAttribute("stocks", stockList);
        verify(model).addAttribute("stockName", "");
        assertEquals("stocks", view);
    }

    @Test
    void showFavouriteStocks_shouldAddFavouritesToModelAndReturnView() {
        List<Stock> favStocks = List.of(new Stock("TSLA", false, null));
        when(userService.getUserFavouriteStocks(email)).thenReturn(favStocks);

        String view = stockController.showFavouriteStocks(model);

        verify(model).addAttribute("stocks", favStocks);
        verify(model).addAttribute("stockName", "");
        assertEquals("favourite-stocks", view);
    }

    @Test
    void addStock_successfulAdd_shouldRedirect() {
        String view = stockController.addStock("MSFT", model);

        verify(userService).addStockToUser(email, "MSFT");
        assertEquals("redirect:/stocks", view);
    }

    @Test
    void addStock_duplicateOrError_shouldReturnFormViewWithError() {
        doThrow(new RuntimeException("Already exists")).when(userService).addStockToUser(email, "AAPL");
        when(userService.getUserStocks(email)).thenReturn(List.of());

        String view = stockController.addStock("AAPL", model);

        verify(model).addAttribute("error", "Already exists");
        verify(model).addAttribute("stockName", "AAPL");
        verify(model).addAttribute("stocks", List.of());
        assertEquals("/api/stocks", view);
    }

    @Test
    void updateStockStatus_shouldUpdateSoldAndFavouriteFlags() {
        Stock stock = new Stock("GOOG", false, new ApplicationUser());
        stock.getUser().setEmail(email);

        when(stockRepository.findById(1L)).thenReturn(Optional.of(stock));

        String view = stockController.updateStockStatus(1L, true, false, model);

        assertEquals("redirect:/api/stocks", view);
        assertEquals(true, stock.isSold());
        assertEquals(false, stock.isFavourite());
        verify(stockRepository).save(stock);
    }

    @Test
    void updateStockStatus_unauthorizedAccess_shouldReturnWithError() {
        Stock stock = new Stock("NFLX", false, new ApplicationUser());
        stock.getUser().setEmail("another@example.com"); // nepatří uživateli

        when(stockRepository.findById(2L)).thenReturn(Optional.of(stock));
        when(userService.getUserStocks(email)).thenReturn(List.of());

        String view = stockController.updateStockStatus(2L, true, null, model);

        verify(model).addAttribute(eq("error"), contains("Unauthorized access"));
        assertEquals("stocks", view);
    }


    @Test
    void updateFromApi_shouldUpdateAndReturnMessage() {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("john@example.com");
        List<Stock> existing = new ArrayList<>();
        Stock s1 = new Stock("AAPL", false, user);
        existing.add(s1);

        Map<String, Object> buyGoogle = Map.of("name", "GOOGL", "sell", 0);
        Map<String, Object> sellApple = Map.of("name", "AAPL", "sell", 1);
        List<Map<String, Object>> recs = List.of(buyGoogle, sellApple);

        when(userService.getAllUsersEntity()).thenReturn(List.of(user));
        when(stockRepository.findByUser(user)).thenReturn(existing);

        String result = stockController.updateFromApi(recs);

        assertEquals("Stocks updated for all users. Processed 2 recommendations, skipped 0 invalid ones.", result); // Aktualizovaný očekávaný výsledek
        verify(stockRepository, times(2)).save(any()); // prodání AAPL + nákup GOOGL
    }
}
