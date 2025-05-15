package com.example.stin_news;

import models.ApplicationUser;
import models.Role;
import repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import services.CustomUserDetailsService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_userFound_returnsUserDetails() {
        String email = "test@example.com";
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);
        user.setPassword("password");
        Role role = new Role();
        role.setName("ROLE_USER");
        user.setRoles(Collections.singletonList(role));

        when(userRepository.findByEmail(email)).thenReturn(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(email));
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void mapRolesToAuthorities_rolesNotEmpty_returnsGrantedAuthorities() {
        List<Role> roles = List.of(
                new Role(2L, "ROLE_ADMIN", Collections.emptyList()), // Používáme konstruktor s parametry
                new Role(3L, "ROLE_EDITOR", Collections.emptyList())  // Používáme konstruktor s parametry
        );
        // Voláme privátní metodu pomocí reflexe, protože Mockito to přímo nepodporuje.
        // Nicméně, lepší přístup je testovat veřejnou metodu loadUserByUsername, která tuto metodu interně volá.
        // Pro demonstraci to zde nechávám, ale v praxi je lepší testovat chování zvenku.
        java.lang.reflect.Method mapRolesToAuthoritiesMethod;
        try {
            mapRolesToAuthoritiesMethod = CustomUserDetailsService.class.getDeclaredMethod("mapRolesToAuthorities", Collection.class);
            mapRolesToAuthoritiesMethod.setAccessible(true);
            Collection<? extends GrantedAuthority> authorities = (Collection<? extends GrantedAuthority>) mapRolesToAuthoritiesMethod.invoke(customUserDetailsService, roles);

            assertNotNull(authorities);
            assertEquals(2, authorities.size());
            assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
            assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_EDITOR")));
        } catch (Exception e) {
            fail("Error during reflection: " + e.getMessage());
        }
    }

    @Test
    void mapRolesToAuthorities_rolesEmpty_returnsEmptyGrantedAuthorities() {
        List<Role> roles = Collections.emptyList();

        // Stejně jako výše, testujeme privátní metodu pomocí reflexe.
        java.lang.reflect.Method mapRolesToAuthoritiesMethod;
        try {
            mapRolesToAuthoritiesMethod = CustomUserDetailsService.class.getDeclaredMethod("mapRolesToAuthorities", Collection.class);
            mapRolesToAuthoritiesMethod.setAccessible(true);
            Collection<? extends GrantedAuthority> authorities = (Collection<? extends GrantedAuthority>) mapRolesToAuthoritiesMethod.invoke(customUserDetailsService, roles);

            assertNotNull(authorities);
            assertTrue(authorities.isEmpty());
        } catch (Exception e) {
            fail("Error during reflection: " + e.getMessage());
        }
    }
}