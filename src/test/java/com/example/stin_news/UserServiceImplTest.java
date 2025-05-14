package com.example.stin_news;


import dtos.UserDto;
import models.ApplicationUser;
import models.Role;
import models.Stock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import repositories.RoleRepository;
import repositories.StockRepository;
import repositories.UserRepository;
import services.UserServiceImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void findUserByEmail_existingUser_returnsApplicationUser() {
        String email = "test@example.com";
        ApplicationUser user = new ApplicationUser();
        when(userRepository.findByEmail(email)).thenReturn(user);

        ApplicationUser result = userService.findUserByEmail(email);

        assertEquals(user, result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void findUserByEmail_nonExistingUser_returnsNull() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        ApplicationUser result = userService.findUserByEmail(email);

        assertNull(result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void saveUser_newRole_savesUserAndRole() {
        UserDto userDto = new UserDto(null, "John", "Doe", "john.doe@example.com", "password");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(null);
        Role savedRole = new Role();
        savedRole.setId(1L);
        savedRole.setName("ROLE_ADMIN");
        when(roleRepository.save(any(Role.class))).thenReturn(savedRole);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        userService.saveUser(userDto);

        verify(roleRepository, times(1)).findByName("ROLE_ADMIN");
        verify(roleRepository, times(1)).save(any(Role.class));
        verify(userRepository, times(1)).save(argThat(user ->
                user.getName().equals("John Doe") &&
                        user.getEmail().equals("john.doe@example.com") &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRoles().contains(savedRole)
        ));
    }

    @Test
    void saveUser_existingRole_savesUserWithExistingRole() {
        UserDto userDto = new UserDto(null, "Jane", "Doe", "jane.doe@example.com", "password");
        Role existingRole = new Role();
        existingRole.setId(1L);
        existingRole.setName("ROLE_ADMIN");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(existingRole);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        userService.saveUser(userDto);

        verify(roleRepository, times(1)).findByName("ROLE_ADMIN");
        verify(roleRepository, never()).save(any(Role.class));
        verify(userRepository, times(1)).save(argThat(user ->
                user.getName().equals("Jane Doe") &&
                        user.getEmail().equals("jane.doe@example.com") &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRoles().contains(existingRole)
        ));
    }

    @Test
    void saveUser_databaseError_throwsRuntimeException() {
        UserDto userDto = new UserDto(null, "Error", "User", "error@example.com", "password");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(null);
        when(roleRepository.save(any(Role.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userService.saveUser(userDto));

        verify(roleRepository, times(1)).findByName("ROLE_ADMIN");
        verify(roleRepository, times(1)).save(any(Role.class));
        verify(userRepository, never()).save(any());
    }

    @Test
    void findAllUsers_usersExist_returnsListOfUserDtos() {
        ApplicationUser user1 = new ApplicationUser();
        user1.setName("John Doe");
        user1.setEmail("john.doe@example.com");
        ApplicationUser user2 = new ApplicationUser();
        user2.setName("Jane Smith");
        user2.setEmail("jane.smith@example.com");
        List<ApplicationUser> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> userDtos = userService.findAllUsers();

        assertEquals(2, userDtos.size());
        assertEquals("John", userDtos.get(0).getFirstName());
        assertEquals("Doe", userDtos.get(0).getLastName());
        assertEquals("john.doe@example.com", userDtos.get(0).getEmail());
        assertEquals("Jane", userDtos.get(1).getFirstName());
        assertEquals("Smith", userDtos.get(1).getLastName());
        assertEquals("jane.smith@example.com", userDtos.get(1).getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findAllUsers_noUsers_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> userDtos = userService.findAllUsers();

        assertTrue(userDtos.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void addStockToUser_userExists_savesNewStock() {
        String email = "test@example.com";
        String stockName = "AAPL";
        ApplicationUser user = new ApplicationUser();
        when(userRepository.findByEmail(email)).thenReturn(user);

        userService.addStockToUser(email, stockName);

        verify(userRepository, times(1)).findByEmail(email);
        verify(stockRepository, times(1)).save(argThat(stock ->
                stock.getName().equals(stockName) &&
                        !stock.isSold() &&
                        stock.getUser().equals(user)
        ));
    }

    @Test
    void addStockToUser_userDoesNotExist_throwsRuntimeException() {
        String email = "test@example.com";
        String stockName = "AAPL";
        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> userService.addStockToUser(email, stockName));

        verify(userRepository, times(1)).findByEmail(email);
        verify(stockRepository, never()).save(any());
    }

    @Test
    void addStockToUser_databaseError_throwsRuntimeException() {
        String email = "test@example.com";
        String stockName = "AAPL";
        ApplicationUser user = new ApplicationUser();
        when(userRepository.findByEmail(email)).thenReturn(user);
        when(stockRepository.save(any(Stock.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userService.addStockToUser(email, stockName));

        verify(userRepository, times(1)).findByEmail(email);
        verify(stockRepository, times(1)).save(any());
    }

    @Test
    void getUserStocks_userExists_returnsListOfStocks() {
        String email = "test@example.com";
        ApplicationUser user = new ApplicationUser();
        Stock stock1 = new Stock("AAPL", false, user);
        Stock stock2 = new Stock("GOOGL", true, user);
        List<Stock> stocks = Arrays.asList(stock1, stock2);
        when(userRepository.findByEmail(email)).thenReturn(user);
        when(stockRepository.findByUser(user)).thenReturn(stocks);

        List<Stock> result = userService.getUserStocks(email);

        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).getName());
        assertEquals("GOOGL", result.get(1).getName());
        verify(userRepository, times(1)).findByEmail(email);
        verify(stockRepository, times(1)).findByUser(user);
    }

    @Test
    void getUserStocks_userDoesNotExist_throwsRuntimeException() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> userService.getUserStocks(email));

        verify(userRepository, times(1)).findByEmail(email);
        verify(stockRepository, never()).findByUser(any());
    }

    @Test
    void getUserFavouriteStocks_userExists_returnsListOfFavouriteStocks() {
        String email = "test@example.com";
        ApplicationUser user = new ApplicationUser();
        Stock favStock1 = new Stock("AAPL", false, user);
        favStock1.setFavourite(true);
        Stock notFavStock = new Stock("GOOGL", true, user);
        List<Stock> favouriteStocks = Collections.singletonList(favStock1);
        when(userRepository.findByEmail(email)).thenReturn(user);
        when(stockRepository.findByUserAndIsFavouriteTrue(user)).thenReturn(favouriteStocks);

        List<Stock> result = userService.getUserFavouriteStocks(email);

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getName());
        assertTrue(result.get(0).isFavourite());
        verify(userRepository, times(1)).findByEmail(email);
        verify(stockRepository, times(1)).findByUserAndIsFavouriteTrue(user);
    }

    @Test
    void getUserFavouriteStocks_userDoesNotExist_throwsUsernameNotFoundException() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.getUserFavouriteStocks(email));

        verify(userRepository, times(1)).findByEmail(email);
        verify(stockRepository, never()).findByUserAndIsFavouriteTrue(any());
    }

    @Test
    void updateStockStatus_stockExists_updatesIsSoldStatus() {
        Long stockId = 1L;
        boolean isSold = true;
        Stock stock = new Stock("AAPL", false, new ApplicationUser());
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);

        userService.updateStockStatus(stockId, isSold);

        assertTrue(stock.isSold());
        verify(stockRepository, times(1)).findById(stockId);
        verify(stockRepository, times(1)).save(stock);
    }

    @Test
    void updateStockStatus_stockDoesNotExist_throwsRuntimeException() {
        Long stockId = 1L;
        boolean isSold = true;
        when(stockRepository.findById(stockId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateStockStatus(stockId, isSold));

        verify(stockRepository, times(1)).findById(stockId);
        verify(stockRepository, never()).save(any());
    }

    @Test
    void updateStockStatus_databaseError_throwsRuntimeException() {
        Long stockId = 1L;
        boolean isSold = true;
        Stock stock = new Stock("AAPL", false, new ApplicationUser());
        when(stockRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userService.updateStockStatus(stockId, isSold));

        verify(stockRepository, times(1)).findById(stockId);
        verify(stockRepository, times(1)).save(stock);
    }

    @Test
    void getAllUsersEntity_usersExist_returnsListOfApplicationUsers() {
        ApplicationUser user1 = new ApplicationUser();
        ApplicationUser user2 = new ApplicationUser();
        List<ApplicationUser> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<ApplicationUser> result = userService.getAllUsersEntity();

        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsersEntity_noUsers_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<ApplicationUser> result = userService.getAllUsersEntity();

        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }
}