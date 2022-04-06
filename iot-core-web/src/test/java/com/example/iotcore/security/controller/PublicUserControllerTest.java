package com.example.iotcore.security.controller;

import com.example.iotcore.config.SecurityConfiguration;
import com.example.iotcore.security.domain.Authority;
import com.example.iotcore.security.dto.UserDTO;
import com.example.iotcore.security.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicUserController.class,
        excludeAutoConfiguration = {SecurityConfiguration.class,
                ManagementWebSecurityAutoConfiguration.class,
                SecurityAutoConfiguration.class})
@ContextConfiguration(classes = PublicUserController.class)
class PublicUserControllerTest {
    private static UserDTO userDTO1;
    private static UserDTO userDTO2;
    private static Authority authority1;
    private static Authority authority2;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;

    @BeforeAll
    static void beforeAll() {
        userDTO1 = new UserDTO(1L, "user1");

        userDTO2 = new UserDTO(2L, "user2");

        authority1 = new Authority("ROLE_ADMIN");


        authority2 = new Authority("ROLE_USER");
    }

    @Test
    void getAllPublicUsers() throws Exception {
        // given
        Page<UserDTO> userDTOS = new PageImpl<>(List.of(userDTO1, userDTO2));
        given(userService.getAllPublicUsers(any())).willReturn(userDTOS);
        // when
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].id").value(userDTO1.getId().intValue()))
                .andExpect(jsonPath("$[0].login").value(userDTO1.getLogin()))
                .andExpect(jsonPath("$[1].id").value(userDTO2.getId().intValue()))
                .andExpect(jsonPath("$[1].login").value(userDTO2.getLogin()));
        // then
        verify(userService, times(1)).getAllPublicUsers(any());
    }

    @Test
    void getAuthorities() throws Exception {
        // given
        given(userService.getAuthorities()).willReturn(List.of(authority1.getName(), authority2.getName()));
        // when
        mockMvc.perform(get("/api/authorities"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0]").value(authority1.getName()))
                .andExpect(jsonPath("$[1]").value(authority2.getName()));
        // then
        verify(userService, times(1)).getAuthorities();
    }
}