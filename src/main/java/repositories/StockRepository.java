package repositories;

import models.Stock;
import models.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByUser(ApplicationUser user);
    List<Stock> findByUserAndIsFavouriteTrue(ApplicationUser user);
    Optional<Stock> findByNameAndUserAndIsSoldFalse(String name, ApplicationUser user);
    boolean existsByNameAndUserAndIsSoldFalse(String name, ApplicationUser user);
}