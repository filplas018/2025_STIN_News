package com.example.stin_news;



import models.ApplicationUser;
import models.Role;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RoleTest {

    @Test
    void testGettersAndSetters() {
        Role role = new Role();
        role.setId(1L);
        role.setName("ROLE_ADMIN");
        List<ApplicationUser> users = new ArrayList<>();
        users.add(new ApplicationUser());
        role.setUsers(users);

        assertEquals(1L, role.getId());
        assertEquals("ROLE_ADMIN", role.getName());
        assertEquals(users, role.getUsers());
    }

    @Test
    void testNoArgsConstructor() {
        Role role = new Role();
        assertNull(role.getId());
        assertNull(role.getName());
        assertNull(role.getUsers());
    }

    @Test
    void testAllArgsConstructor() {
        List<ApplicationUser> users = new ArrayList<>();
        users.add(new ApplicationUser(1L, "Test User", "test@example.com", "password", new ArrayList<>()));
        Role role = new Role(2L, "ROLE_USER", users);

        assertEquals(2L, role.getId());
        assertEquals("ROLE_USER", role.getName());
        assertEquals(users, role.getUsers());
    }

    @Test
    void testSetAndGetId() {
        Role role = new Role();
        role.setId(3L);
        assertEquals(3L, role.getId());
    }

    @Test
    void testSetAndGetName() {
        Role role = new Role();
        role.setName("ROLE_EDITOR");
        assertEquals("ROLE_EDITOR", role.getName());
    }


    @Test
    void testEmptyUsersListByDefault() {
        Role role = new Role();
        assertNull(role.getUsers()); // AllArgsConstructor může inicializovat na null
    }

    @Test
    void testAllArgsConstructorWithNullUsers() {
        Role role = new Role(5L, "ROLE_GUEST", null);
        assertEquals(5L, role.getId());
        assertEquals("ROLE_GUEST", role.getName());
        assertNull(role.getUsers());
    }
}