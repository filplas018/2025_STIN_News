package com.example.stin_news;


import models.ApplicationUser;
import models.Role;
import models.Stock;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationUserTest {

    @Test
    void testGettersAndSetters() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");
        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ROLE_USER");

        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("ROLE_ADMIN");

        List<Role> roles = Arrays.asList(role1, role2);
        user.setRoles(roles);
        List<Stock> stocks = Arrays.asList(new Stock(), new Stock());
        user.setStocks(stocks);

        assertEquals(1L, user.getId());
        assertEquals("John Doe", user.getName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals(roles, user.getRoles());
        assertEquals(stocks, user.getStocks());
    }

    @Test
    void testGetAuthorities() {
        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ROLE_USER");

        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("ROLE_ADMIN");

        List<Role> roles = Arrays.asList(role1, role2);
        ApplicationUser user = new ApplicationUser();
        user.setRoles(roles);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void testGetUsername() {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        assertEquals("test@example.com", user.getUsername());
    }

    @Test
    void testGetPassword() {
        ApplicationUser user = new ApplicationUser();
        user.setPassword("securePassword");
        assertEquals("securePassword", user.getPassword());
    }

    @Test
    void testIsAccountNonExpired() {
        ApplicationUser user = new ApplicationUser();
        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        ApplicationUser user = new ApplicationUser();
        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        ApplicationUser user = new ApplicationUser();
        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        ApplicationUser user = new ApplicationUser();
        assertTrue(user.isEnabled());
    }

    @Test
    void testNoArgsConstructor() {
        ApplicationUser user = new ApplicationUser();
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
        assertNotNull(user.getStocks());
        assertTrue(user.getStocks().isEmpty());
    }

    @Test
    void testAllArgsConstructor() {
        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ROLE_USER");

        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("ROLE_ADMIN");

        List<Role> roles = Arrays.asList(role1, role2);
        Stock stock1 = new Stock();
        Stock stock2 = new Stock();
        List<Stock> stocks = Arrays.asList(stock1, stock2);

        ApplicationUser user = new ApplicationUser(1L, "Full Name", "full.name@example.com", "complexPassword", roles, stocks);

        assertEquals(1L, user.getId());
        assertEquals("Full Name", user.getName());
        assertEquals("full.name@example.com", user.getEmail());
        assertEquals("complexPassword", user.getPassword());
        assertEquals(roles, user.getRoles());
        assertEquals(stocks, user.getStocks());
    }

    @Test
    void testUserDetailsMethodsReturnTrueByDefault() {
        ApplicationUser user = new ApplicationUser();
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }
}