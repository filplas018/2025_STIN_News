package dtos;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import services.StockQueryDtoDeserializer;



@JsonDeserialize(using = StockQueryDtoDeserializer.class)
public class StockQueryDto {
    private String name;
    private long date;
    private Integer rating;
    private Integer sell;



    public StockQueryDto(String name, int rating) {
        this.name = name;
        this.rating = rating;
    }

    public StockQueryDto() {

    }

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Integer getSell() { return sell; }
    public void setSell(Integer sell) { this.sell = sell; }
}
