package com.example.salesforcecrud.controller;

import com.example.salesforcecrud.config.SalesforceObjectConfig;
import com.example.salesforcecrud.service.RecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ObjectController.class)
class ObjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordService recordService;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(c -> c.disable())
                .authorizeHttpRequests(a -> a.anyRequest().permitAll());
            return http.build();
        }
    }

    @Test
    void getSupportedObjectsShouldReturn200() throws Exception {
        when(recordService.getSupportedObjects())
                .thenReturn(List.of("Account", "Contact", "Lead", "Opportunity", "Case"));

        mockMvc.perform(get("/api/objects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.objects").isArray())
                .andExpect(jsonPath("$.objects.length()").value(5));
    }
}
