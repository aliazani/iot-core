package com.example.iotcore.security.controller;

import com.example.iotcore.config.SecurityConfiguration;
import com.example.iotcore.security.domain.Authority;
import com.example.iotcore.security.domain.User;
import com.example.iotcore.security.dto.AdminUserDTO;
import com.example.iotcore.security.mapper.UserMapper;
import com.example.iotcore.security.repository.UserRepository;
import com.example.iotcore.security.service.MailService;
import com.example.iotcore.security.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeAutoConfiguration = {SecurityConfiguration.class,
                ManagementWebSecurityAutoConfiguration.class,
                SecurityAutoConfiguration.class})
@ContextConfiguration(classes = UserController.class)
@ActiveProfiles("test")
class UserControllerTest {
    private static final String ENTITY_API_URL = "/api/admin/users";
    private static final String ENTITY_API_URL_LOGIN = ENTITY_API_URL + "/{login}";
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserService userService;
    @MockBean
    UserRepository userRepository;
    @MockBean
    UserMapper userMapper;
    @MockBean
    MailService mailService;

    private AdminUserDTO adminUserDTO1;
    private AdminUserDTO adminUserDTO2;
    private User user1;

    @BeforeEach
    void setUp() {
        adminUserDTO1 = AdminUserDTO.builder()
                .id(1L)
                .login("user1")
                .firstName("firstname")
                .lastName("lastname")
                .email("user-1@localhost.com")
                .imageUrl("http://placehold.it/50x50")
                .langKey("EN")
                .authorities(Set.of("ROLE_USER"))
                .build();

        user1 = User.builder()
                .id(adminUserDTO1.getId())
                .login(adminUserDTO1.getLogin())
                .firstName(adminUserDTO1.getFirstName())
                .lastName(adminUserDTO1.getLastName())
                .email(adminUserDTO1.getEmail())
                .imageUrl(adminUserDTO1.getImageUrl())
                .langKey(adminUserDTO1.getLangKey())
                .activated(true)
                .authorities(Set.of(new Authority("ROLE_USER")))
                .password("password")
                .resetKey("resetKey")
                .resetDate(Instant.now())
                .build();

        adminUserDTO2 = AdminUserDTO.builder()
                .id(2L)
                .login("user2")
                .firstName("firstname-2")
                .lastName("lastname-2")
                .email("user-2@localhost.com")
                .imageUrl("http://placehold.it/50x50-2")
                .langKey("EN")
                .activated(true)
                .authorities(Set.of("ROLE_ADMIN"))
                .build();
    }

    @Test
    void createUser() throws Exception {
        // given
        AdminUserDTO adminUserDTO = AdminUserDTO.builder()
                .login(adminUserDTO1.getLogin())
                .firstName(adminUserDTO1.getFirstName())
                .lastName(adminUserDTO1.getLastName())
                .email(adminUserDTO1.getEmail())
                .imageUrl(adminUserDTO1.getImageUrl())
                .langKey(adminUserDTO1.getLangKey())
                .authorities(adminUserDTO1.getAuthorities())
                .build();
        given(userService.createUser(adminUserDTO)).willReturn(user1);

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUserDTO))
                )
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(user1.getId().intValue()))
                .andExpect(jsonPath("$.login").value(user1.getLogin()))
                .andExpect(jsonPath("$.firstName").value(user1.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user1.getLastName()))
                .andExpect(jsonPath("$.email").value(user1.getEmail()))
                .andExpect(jsonPath("$.imageUrl").value(user1.getImageUrl()))
                .andExpect(jsonPath("$.langKey").value(user1.getLangKey()))
                .andExpect(jsonPath("$.activated").value(user1.isActivated()))
        ;

        // then
        verify(userService, times(1)).createUser(adminUserDTO);
    }

    @Test
    void updateUser() throws Exception {
        // given
        AdminUserDTO adminUserDTO = AdminUserDTO.builder()
                .id(adminUserDTO1.getId())
                .login(adminUserDTO1.getLogin())
                .firstName("updatedFirstName")
                .lastName("updatedLastName")
                .email(adminUserDTO1.getEmail())
                .imageUrl(adminUserDTO1.getImageUrl())
                .activated(true)
                .langKey(adminUserDTO1.getLangKey())
                .authorities(Set.of("ROLE_ADMIN", "ROLE_USER"))
                .build();

        given(userService.updateUser(adminUserDTO)).willReturn(Optional.of(adminUserDTO));

        // when
        mockMvc.perform(put(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUserDTO))
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(adminUserDTO.getId().intValue()))
                .andExpect(jsonPath("$.login").value(adminUserDTO.getLogin()))
                .andExpect(jsonPath("$.firstName").value(adminUserDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(adminUserDTO.getLastName()))
                .andExpect(jsonPath("$.email").value(adminUserDTO.getEmail()))
                .andExpect(jsonPath("$.imageUrl").value(adminUserDTO.getImageUrl()))
                .andExpect(jsonPath("$.langKey").value(adminUserDTO.getLangKey()))
                .andExpect(jsonPath("$.activated").value(adminUserDTO.isActivated()))
                .andExpect(jsonPath("$.authorities", containsInAnyOrder("ROLE_ADMIN", "ROLE_USER")))
        ;

        // then
        verify(userService, times(1)).updateUser(adminUserDTO);
    }

    @Test
    void getAllUsers() throws Exception {
        // given
        adminUserDTO1.setActivated(true);
        Page<AdminUserDTO> adminUserDTOs = new PageImpl<>(List.of(adminUserDTO1, adminUserDTO2));
        given(userService.getAllManagedUsers(any(Pageable.class))).willReturn(adminUserDTOs);

        // when
        mockMvc.perform(get(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[0].id").value(adminUserDTO1.getId().intValue()))
                .andExpect(jsonPath("$.[0].login").value(adminUserDTO1.getLogin()))
                .andExpect(jsonPath("$.[0].firstName").value(adminUserDTO1.getFirstName()))
                .andExpect(jsonPath("$.[0].lastName").value(adminUserDTO1.getLastName()))
                .andExpect(jsonPath("$.[0].email").value(adminUserDTO1.getEmail()))
                .andExpect(jsonPath("$.[0].imageUrl").value(adminUserDTO1.getImageUrl()))
                .andExpect(jsonPath("$.[0].langKey").value(adminUserDTO1.getLangKey()))
                .andExpect(jsonPath("$.[0].activated").value(adminUserDTO1.isActivated()))
                .andExpect(jsonPath("$.[0].authorities", containsInAnyOrder("ROLE_USER")))

                .andExpect(jsonPath("$.[1].id").value(adminUserDTO2.getId().intValue()))
                .andExpect(jsonPath("$.[1].login").value(adminUserDTO2.getLogin()))
                .andExpect(jsonPath("$.[1].firstName").value(adminUserDTO2.getFirstName()))
                .andExpect(jsonPath("$.[1].lastName").value(adminUserDTO2.getLastName()))
                .andExpect(jsonPath("$.[1].email").value(adminUserDTO2.getEmail()))
                .andExpect(jsonPath("$.[1].imageUrl").value(adminUserDTO2.getImageUrl()))
                .andExpect(jsonPath("$.[1].langKey").value(adminUserDTO2.getLangKey()))
                .andExpect(jsonPath("$.[1].activated").value(adminUserDTO2.isActivated()))
                .andExpect(jsonPath("$.[1].authorities", containsInAnyOrder("ROLE_ADMIN")))
        ;

        // then
        verify(userService, times(1)).getAllManagedUsers(any(Pageable.class));
    }

    @Test
    void getUser() throws Exception {
        // given
        adminUserDTO1.setActivated(true);
        given(userService.getUserWithAuthoritiesByLogin(adminUserDTO1.getLogin())).willReturn(Optional.of(user1));
        given(userMapper.toAdminUserDTO(user1)).willReturn(adminUserDTO1);

        // when
        mockMvc.perform(get(ENTITY_API_URL_LOGIN, adminUserDTO1.getLogin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(user1.getId().intValue()))
                .andExpect(jsonPath("$.login").value(user1.getLogin()))
                .andExpect(jsonPath("$.firstName").value(user1.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user1.getLastName()))
                .andExpect(jsonPath("$.email").value(user1.getEmail()))
                .andExpect(jsonPath("$.imageUrl").value(user1.getImageUrl()))
                .andExpect(jsonPath("$.langKey").value(user1.getLangKey()))
                .andExpect(jsonPath("$.activated").value(user1.isActivated()))
                .andExpect(jsonPath("$.authorities", containsInAnyOrder("ROLE_USER")));

        // then
        verify(userService, times(1)).getUserWithAuthoritiesByLogin(adminUserDTO1.getLogin());
    }

    @Test
    void deleteUser() throws Exception {
        // given
        // when
        mockMvc.perform(delete(ENTITY_API_URL_LOGIN, adminUserDTO1.getLogin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        // then
        verify(userService, times(1)).deleteUser(adminUserDTO1.getLogin());
    }
}