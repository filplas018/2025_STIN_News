package com.example.stin_news;

import controllers.AuthController;
import dtos.UserDto;
import models.ApplicationUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private BindingResult result;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void homePage_returnsIndexView() {
        String viewName = authController.home();
        assertEquals("index", viewName);
    }

    @Test
    void showRegistrationForm_addsUserDtoToModelAndReturnsRegisterView() {
        String viewName = authController.showRegistrationForm(model);
        assertEquals("/register", viewName);
        verify(model).addAttribute(eq("user"), any(UserDto.class));
    }

    @Test
    void registration_validUser_savesUserAndRedirectsToLogin() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");
        when(result.hasErrors()).thenReturn(false);
        when(userService.findUserByEmail(userDto.getEmail())).thenReturn(null);

        String viewName = authController.registration(userDto, result, model);

        verify(userService).saveUser(userDto);
        assertEquals("redirect:/login", viewName);
    }

    /*@Test
    void registration_existingUser_addsErrorToBindingResultAndReturnsRegisterView() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");
        ApplicationUser existingUser = new ApplicationUser();
        existingUser.setEmail("test@example.com");
        when(result.hasErrors()).thenReturn(false); // Na začátku žádné chyby
        when(userService.findUserByEmail(userDto.getEmail())).thenReturn(existingUser);

        String viewName = authController.registration(userDto, result, model);

        verify(userService, never()).saveUser(userDto);
        assertEquals("/register", viewName);
        verify(result).rejectValue(eq("email"), isNull(), eq("There is already an account registered with the same email"));
        verify(model).addAttribute(eq("user"), eq(userDto));
        // Dodatečné ověření, že result má chyby
        when(result.hasErrors()).thenReturn(true); // Nastavíme, že po volání rejectValue by měly být chyby
        assertTrue(result.hasErrors());
    }*/

    @Test
    void registration_validationErrors_addsUserDtoToModelAndReturnsRegisterView() {
        UserDto userDto = new UserDto();
        when(result.hasErrors()).thenReturn(true);

        String viewName = authController.registration(userDto, result, model);

        verify(userService, never()).saveUser(userDto);
        assertEquals("/register", viewName);
        verify(model).addAttribute(eq("user"), eq(userDto));
        verify(result).getAllErrors(); // Ověřujeme, že se volají chyby validace pro logování
    }

    /*@Test
    void registration_saveUserThrowsException_addsErrorToModelAndReturnsRegisterView() {
        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");
        when(result.hasErrors()).thenReturn(false);
        when(userService.findUserByEmail(userDto.getEmail())).thenReturn(null);
        when(userService.saveUser(userDto)).thenThrow(new RuntimeException("Simulated save error"));

        String viewName = authController.registration(userDto, result, model);

        assertEquals("/register", viewName);
        verify(model).addAttribute(eq("user"), eq(userDto));
        verify(model).addAttribute(eq("error"), eq("Registration failed: Simulated save error"));
    }*/

    @Test
    void logout_authenticatedUser_logsOutAndRedirectsToLoginWithLogoutParam() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        String viewName = authController.logout(request, response);

        // Ověřujeme, že se získala autentikace z SecurityContextHolder
        verify(securityContext, times(1)).getAuthentication();
        assertEquals("redirect:/login?logout", viewName);

        SecurityContextHolder.clearContext(); // Čistíme kontext po testu
    }

    @Test
    void logout_unauthenticatedUser_redirectsToLoginWithLogoutParam() {
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        String viewName = authController.logout(request, response);

        // Ověřujeme, že se získala autentikace z SecurityContextHolder
        verify(securityContext, times(1)).getAuthentication();
        assertEquals("redirect:/login?logout", viewName);

        SecurityContextHolder.clearContext(); // Čistíme kontext po testu
    }

    @Test
    void users_returnsUsersViewWithUserList() {
        List<UserDto> usersList = Collections.singletonList(new UserDto());
        when(userService.findAllUsers()).thenReturn(usersList);

        String viewName = authController.users(model);

        assertEquals("users", viewName);
        verify(model).addAttribute(eq("users"), eq(usersList));
    }

    @Test
    void loginPage_returnsLoginView() {
        String viewName = authController.login();
        assertEquals("login", viewName);
    }
}