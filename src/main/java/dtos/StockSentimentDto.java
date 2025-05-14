package dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;


public class StockSentimentDto {

    @NotBlank
    private String stockName;

    @Min(-10)
    @Max(10)
    private int rating;

    @NotNull
    private LocalDateTime validFrom;

    private String articleText; // Přidáno

    // --- Gettery a settery ---

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public String getArticleText() {
        return articleText;
    }

    public void setArticleText(String articleText) {
        this.articleText = articleText;
    }
}