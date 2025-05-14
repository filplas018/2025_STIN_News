package services;

import dtos.UserDto;
import models.ApplicationUser;
import models.Role;
import models.Stock;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import repositories.RoleRepository;
import repositories.StockRepository;
import repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StockRepository stockRepository;
    private final PasswordEncoder passwordEncoder;


    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           StockRepository stockRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.stockRepository = stockRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ApplicationUser findUserByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public void saveUser(UserDto userDto) {
        log.debug("Attempting to save user: {}", userDto);
        try {
            ApplicationUser user = new ApplicationUser();
            user.setName(userDto.getFirstName() + " " + userDto.getLastName());
            user.setEmail(userDto.getEmail());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));

            Role role = roleRepository.findByName("ROLE_ADMIN");
            if (role == null) {
                role = checkRoleExist();
            }
            user.setRoles(Arrays.asList(role));
            userRepository.save(user);
            log.info("User saved successfully: {}", userDto);
        } catch (Exception e) {
            log.error("Error saving user: {}", userDto, e);
            throw new RuntimeException("Failed to save user", e);
        }
    }

    private Role checkRoleExist() {
        log.debug("Checking if ROLE_ADMIN exists");
        try {
            Role role = new Role();
            role.setName("ROLE_ADMIN");
            Role savedRole = roleRepository.save(role);
            log.info("Role ROLE_ADMIN created successfully");
            return savedRole;
        } catch (Exception e) {
            log.error("Error creating ROLE_ADMIN", e);
            throw new RuntimeException("Failed to create role", e);
        }
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<ApplicationUser> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToUserDto(ApplicationUser user) {
        UserDto userDto = new UserDto();
        String[] str = user.getName().split(" ");
        userDto.setFirstName(str[0]);
        userDto.setLastName(str[1]);
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    @Override
    @Transactional
    public void addStockToUser(String email, String stockName) {
        log.debug("Adding stock {} to user with email {}", stockName, email);
        try {
            ApplicationUser user = userRepository.findByEmail(email);
            if (user == null) {
                throw new RuntimeException("User not found: " + email);
            }
            Stock stock = new Stock(stockName, false, user);
            stockRepository.save(stock);
            log.info("Stock {} added to user {}", stockName, email);
        } catch (Exception e) {
            log.error("Error adding stock {} to user {}", stockName, email, e);
            throw new RuntimeException("Failed to add stock", e);
        }
    }

    @Override
    public List<Stock> getUserStocks(String email) {
        log.debug("Fetching stocks for user with email {}", email);
        ApplicationUser user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found: " + email);
        }
        return stockRepository.findByUser(user);
    }

    public List<Stock> getUserFavouriteStocks(String email) {
        Optional<ApplicationUser> userOptional = Optional.ofNullable(userRepository.findByEmail(email));
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User with email " + email + " not found");
        }

        ApplicationUser user = userOptional.get();
        return stockRepository.findByUserAndIsFavouriteTrue(user);
    }

    @Override
    @Transactional
    public void updateStockStatus(Long stockId, boolean isSold) {
        log.debug("Updating stock {} to sold status: {}", stockId, isSold);
        try {
            Stock stock = stockRepository.findById(stockId)
                    .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));
            stock.setSold(isSold);
            stockRepository.save(stock);
            log.info("Stock {} updated to sold status: {}", stockId, isSold);
        } catch (Exception e) {
            log.error("Error updating stock {} status", stockId, e);
            throw new RuntimeException("Failed to update stock status", e);
        }
    }

    @Override
    public List<ApplicationUser> getAllUsersEntity() {
        return userRepository.findAll();
    }




}