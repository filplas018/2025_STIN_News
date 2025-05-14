package controllers;

import dtos.StockQueryDto;
import models.ApplicationUser;
import models.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import repositories.StockRepository;
import services.UserService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/stocks")
public class StockController {

    private final UserService userService;
    private final StockRepository stockRepository;


    private static final Logger logger = LoggerFactory.getLogger(SentimentController.class);

    public StockController(UserService userService, StockRepository stockRepository) {
        this.userService = userService;
        this.stockRepository = stockRepository;
    }

    @GetMapping({"", "/list"})
    public String showStocks(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Stock> stocks = userService.getUserStocks(email);
        model.addAttribute("stocks", stocks);
        model.addAttribute("stockName", "");
        return "stocks";
    }

    @GetMapping({"favourite", "/list/favourite"})
    public String showFavouriteStocks(Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Stock> favouriteStocks = userService.getUserFavouriteStocks(email); // <-- změněno
        model.addAttribute("stocks", favouriteStocks);
        model.addAttribute("stockName", "");
        return "favourite-stocks";
    }

    @PostMapping("/add")
    public String addStock(@RequestParam String stockName, Model model) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            userService.addStockToUser(email, stockName);
            return "redirect:/stocks";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("stockName", stockName);
            model.addAttribute("stocks", userService.getUserStocks(email));
            return "/api/stocks";
        }
    }

    @PostMapping("/update/{id}")
    public String updateStockStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean isSold, // Volitelné parametry
            @RequestParam(required = false) Boolean isFavourite,
            Model model) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            Stock stock = stockRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Stock not found: " + id));

            // Zkontroluj, zda uživatel vlastní tuto akcii
            if (stock.getUser() == null || !stock.getUser().getEmail().equals(email)) {
                throw new RuntimeException("Unauthorized access to stock: " + id);
            }

            // Pokud je parametr isSold přítomen, aktualizuj hodnotu isSold
            if (isSold != null) {
                stock.setSold(isSold);
            }

            // Pokud je parametr isFavourite přítomen, aktualizuj hodnotu isFavourite
            if (isFavourite != null) {
                stock.setFavourite(isFavourite);
            }

            // Ulož změny do databáze
            stockRepository.save(stock);

            return "redirect:/api/stocks"; // Přesměrování na seznam akcií
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("stockName", "");
            model.addAttribute("stocks", userService.getUserStocks(email));
            return "stocks";
        }
    }

    @PostMapping("/updateFromApi")
    @ResponseBody
    public String updateFromApi(@RequestBody List<Map<String, Object>> recommendations) {
        List<ApplicationUser> users = userService.getAllUsersEntity();
        int processedRecommendations = 0;
        int skippedRecommendations = 0;

        List<Map<String, Object>> validRecommendations = recommendations.stream()
                .filter(rec -> rec != null)
                .collect(Collectors.toList());

        for (ApplicationUser user : users) {
            List<Stock> userStocks = stockRepository.findByUser(user);

            for (Map<String, Object> rec : validRecommendations) {
                String name = (String) rec.get("name");
                int sell = (int) rec.get("sell");

                if (sell == 1) {
                    // Prodej
                    userStocks.stream()
                            .filter(s -> s.getName().equalsIgnoreCase(name) && !s.isSold())
                            .forEach(s -> {
                                s.setSold(true);
                                stockRepository.save(s);
                            });
                    processedRecommendations++;
                } else if (sell == 0) {
                    // Nákup
                    boolean alreadyHas = userStocks.stream()
                            .anyMatch(s -> s.getName().equalsIgnoreCase(name) && !s.isSold());
                    if (!alreadyHas) {
                        Stock newStock = new Stock(name, false, user);
                        stockRepository.save(newStock);
                        processedRecommendations++;
                    }
                }
            }
        }

        logger.info("Zpracováno {} doporučení, přeskočeno {} neplatných doporučení.", processedRecommendations, skippedRecommendations);
        return String.format("Stocks updated for all users. Processed %d recommendations, skipped %d invalid ones.", processedRecommendations, skippedRecommendations);
    }
}