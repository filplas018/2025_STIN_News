package services;

import dtos.UserDto;
import models.ApplicationUser;
import models.Stock;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    ApplicationUser findUserByEmail(String email);

    List<UserDto> findAllUsers();
    List<ApplicationUser> getAllUsersEntity();
    void addStockToUser(String email, String stockName);
    List<Stock> getUserStocks(String email);
    List<Stock> getUserFavouriteStocks(String email);

    void updateStockStatus(Long stockId, boolean isSold);


}