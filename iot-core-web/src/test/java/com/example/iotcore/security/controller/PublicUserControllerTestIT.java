package com.example.iotcore.security.controller;

import com.example.iotcore.MySqlExtension;
import com.example.iotcore.security.domain.Authority;
import com.example.iotcore.security.dto.UserDTO;
import com.example.iotcore.security.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WithMockUser
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicUserControllerTestIT extends MySqlExtension {
    private static final UserDTO userDTO1 = new UserDTO(1L, "admin");
    private static final UserDTO userDTO2 = new UserDTO(2L, "user");
    private static final UserDTO userDTO3 = new UserDTO(3L, "user-admin");
    private static final Authority authority1 = new Authority("ROLE_ADMIN");
    private static final Authority authority2 = new Authority("ROLE_USER");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserService userService;

    @Test
    void getAllPublicUsers() throws Exception {
        // given

        // when
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].id").value(userDTO1.getId().intValue()))
                .andExpect(jsonPath("$[0].login").value(userDTO1.getLogin()))
                .andExpect(jsonPath("$[1].id").value(userDTO2.getId().intValue()))
                .andExpect(jsonPath("$[1].login").value(userDTO2.getLogin()))
                .andExpect(jsonPath("$[2].id").value(userDTO3.getId().intValue()))
                .andExpect(jsonPath("$[2].login").value(userDTO3.getLogin()));

        // then
        assertThat(userService.getAllPublicUsers(any())).hasSize(4);
    }

    @Test
    void getAuthorities() throws Exception {
        // given

        // when
        mockMvc.perform(get("/api/authorities"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0]").value(authority1.getName()))
                .andExpect(jsonPath("$[1]").value(authority2.getName()));
        // then
        assertThat(userService.getAuthorities()).hasSize(2);
    }
}