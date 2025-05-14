package models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stocks")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isSold = false;

    @Column(nullable = false)
    private boolean isFavourite = false;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private ApplicationUser user;

    public Stock(String name, boolean isSold, ApplicationUser user) {
        this.name = name;
        this.isSold = isSold;
        this.user = user;
    }

    // Getter for id
    public Long getId() {
        return id;
    }

    // Setter for id
    public void setId(Long id) {
        this.id = id;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Setter for name
    public void setName(String name) {
        this.name = name;
    }

    // Getter for isSold
    public boolean isSold() {
        return isSold;
    }

    // Setter for isSold
    public void setSold(boolean sold) {
        isSold = sold;
    }

    // Getter for isFavourite
    public boolean isFavourite() {
        return isFavourite;
    }

    // Setter for isFavourite
    public void setFavourite(boolean favourite) {
        isFavourite = favourite;
    }

    // Getter for user
    public ApplicationUser getUser() {
        return user;
    }

    // Setter for user
    public void setUser(ApplicationUser user) {
        this.user = user;
    }
}
