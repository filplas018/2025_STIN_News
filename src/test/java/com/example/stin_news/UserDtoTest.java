package com.example.stin_news;


import dtos.UserDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class UserDtoTest {

    private static Validator v;

    @BeforeAll
    static void setUpV() {
        ValidatorFactory vf = Validation.buildDefaultValidatorFactory();
        v = vf.getValidator();
    }

    @Test
    void testValidDto() {
        UserDto dto = new UserDto(1L, "Filip", "Novák", "filip.novak@example.com", "heslo123");
        Set<ConstraintViolation<UserDto>> violations = v.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNotEmptyFirstName() {
        UserDto dto = new UserDto(1L, "", "Novák", "test@example.com", "password");
        Set<ConstraintViolation<UserDto>> violations = v.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("must not be empty", violations.iterator().next().getMessage());
    }

    @Test
    void testNotEmptyLastName() {
        UserDto dto = new UserDto(1L, "Filip", "", "test@example.com", "password");
        Set<ConstraintViolation<UserDto>> violations = v.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("must not be empty", violations.iterator().next().getMessage());
    }

    @Test
    void testNotEmptyEmail() {
        UserDto dto = new UserDto(1L, "Filip", "Novák", "", "password");
        Set<ConstraintViolation<UserDto>> violations = v.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Email should not be empty", violations.iterator().next().getMessage());
    }

    @Test
    void testInvalidEmailFormat() {
        UserDto dto = new UserDto(1L, "Filip", "Novák", "invalid-email", "password");
        Set<ConstraintViolation<UserDto>> violations = v.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("must be a well-formed email address", violations.iterator().next().getMessage());
    }

    @Test
    void testNotEmptyPassword() {
        UserDto dto = new UserDto(1L, "Filip", "Novák", "test@example.com", "");
        Set<ConstraintViolation<UserDto>> violations = v.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals("Password should not be empty", violations.iterator().next().getMessage());
    }

    @Test
    void testGettersSettersAllArgsConstructor() {
        UserDto dto = new UserDto(2L, "Jana", "Svobodová", "jana.svobodova@example.com", "heslo456");
        assertEquals(2L, dto.getId());
        assertEquals("Jana", dto.getFirstName());
        assertEquals("Svobodová", dto.getLastName());
        assertEquals("jana.svobodova@example.com", dto.getEmail());
        assertEquals("heslo456", dto.getPassword());
    }

    @Test
    void testNoArgsConstructorSetters() {
        UserDto dto = new UserDto();
        dto.setId(3L);
        dto.setFirstName("Petr");
        dto.setLastName("Dvořák");
        dto.setEmail("petr.dvorak@example.com");
        dto.setPassword("tajne789");
        assertEquals(3L, dto.getId());
        assertEquals("Petr", dto.getFirstName());
        assertEquals("Dvořák", dto.getLastName());
        assertEquals("petr.dvorak@example.com", dto.getEmail());
        assertEquals("tajne789", dto.getPassword());
    }
}
