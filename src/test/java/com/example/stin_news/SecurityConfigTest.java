package com.example.stin_news;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import services.UserService;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService; // Mockujeme UserService




    //@Test
//    void apiPathsShouldRequireAuthentication() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/stocks"))
//                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
//                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/login"));
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/stocks/add"))
//                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
//                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/login"));
//    }



    @Test
    void otherAuthenticatedPathsShouldBeAccessible() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/some-authenticated-path"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/login"));

        mockMvc.perform(MockMvcRequestBuilders.get("/some-other-authenticated-path"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/login"));
    }

    @WithMockUser("testuser")
    @Test
    void authenticatedUserShouldAccessOtherAuthenticatedPaths() throws Exception {
        // Ujistěte se, že tyto cesty ve vaší aplikaci skutečně existují a jsou zabezpečené
        mockMvc.perform(MockMvcRequestBuilders.get("/some-authenticated-path"))
                .andExpect(MockMvcResultMatchers.status().isNotFound()); // Pokud cesta neexistuje, očekáváme 404

        mockMvc.perform(MockMvcRequestBuilders.get("/some-other-authenticated-path"))
                .andExpect(MockMvcResultMatchers.status().isNotFound()); // Pokud cesta neexistuje, očekáváme 404
    }

    @Test
    void loginPageShouldBeAccessible() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void logoutPathShouldRedirectToLoginPageWithLogoutParam() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/logout")
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login?logout"));
    }

    @Test
    void csrfShouldBeIgnoredForSentimentEndpoints() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/sentiment/ratings")
                        .content("[{\"name\": \"AAPL\", \"rating\": 5}]")
                        .contentType("application/json"))
                .andExpect(MockMvcResultMatchers.status().isOk()); // Zkontrolujte, zda endpoint skutečně vrací 200 pro validní data
    }
}