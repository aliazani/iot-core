package com.example.iotcore.security.controller;

import com.example.iotcore.MySqlExtension;
import com.example.iotcore.security.controller.vm.LoginVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class UserJWTControllerTestIT extends MySqlExtension {
    private static final String ENTITY_API_URL = "/api/authenticate";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authorize_validCredentials() throws Exception {
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername("admin");
        loginVM.setPassword("admin");

        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginVM))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_token").isString())
                .andExpect(jsonPath("$.id_token").isNotEmpty())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, not(nullValue())))
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, not(is(emptyString()))));
    }

    @Test
    void authorize_validCredentials_rememberMe() throws Exception {
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername("admin");
        loginVM.setPassword("admin");
        loginVM.setRememberMe(true);

        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginVM))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id_token").isString())
                .andExpect(jsonPath("$.id_token").isNotEmpty())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, not(nullValue())))
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, not(is(emptyString()))));
    }


    @Test
    void authorize_invalidCredentials() throws Exception {
        LoginVM loginVM = new LoginVM();
        loginVM.setUsername("wrong");
        loginVM.setPassword("wrong");
        loginVM.setRememberMe(true);

        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginVM))
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.id_token").doesNotExist())
                .andExpect(header().doesNotExist("Authorization"));
    }
}