package com.example.iotcore.security.controller;

import com.example.iotcore.config.SecurityConfiguration;
import com.example.iotcore.security.controller.vm.KeyAndPasswordVM;
import com.example.iotcore.security.controller.vm.ManagedUserVM;
import com.example.iotcore.security.domain.Authority;
import com.example.iotcore.security.domain.User;
import com.example.iotcore.security.dto.AdminUserDTO;
import com.example.iotcore.security.dto.PasswordChangeDTO;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = AccountController.class,
        excludeAutoConfiguration = {SecurityConfiguration.class,
                ManagementWebSecurityAutoConfiguration.class,
                SecurityAutoConfiguration.class})
@ContextConfiguration(classes = AccountController.class)
class AccountControllerTest {
    private final ManagedUserVM managedUserVM = new ManagedUserVM();
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserRepository userRepository;
    @MockBean
    UserMapper userMapper;
    @MockBean
    UserService userService;
    @MockBean
    MailService mailService;
    private User createdUser;
    private User activatedUser;
    private AdminUserDTO adminUserDTO;

    @BeforeEach
    void setUp() {
        managedUserVM.setLogin("user");
        managedUserVM.setEmail("user@email.com");
        managedUserVM.setLangKey("en");
        managedUserVM.setPassword("password");

        createdUser = User.builder()
                .id(1L)
                .login(managedUserVM.getLogin())
                .email(managedUserVM.getEmail())
                .langKey(managedUserVM.getLangKey())
                .password(managedUserVM.getPassword())
                .activated(false)
                .activationKey("activationKey")
                .authorities(Set.of(new Authority("ROLE_USER")))
                .build();

        activatedUser = User.builder()
                .id(createdUser.getId())
                .login(createdUser.getLogin())
                .email(createdUser.getEmail())
                .firstName("firstName")
                .lastName("lastName")
                .langKey(createdUser.getLangKey())
                .password(createdUser.getPassword())
                .authorities(Set.of(new Authority("ROLE_USER")))
                .activated(true)
                .activationKey(null)
                .build();

        adminUserDTO = AdminUserDTO.builder()
                .id(activatedUser.getId())
                .login(activatedUser.getLogin())
                .email(activatedUser.getEmail())
                .firstName(activatedUser.getFirstName())
                .lastName(activatedUser.getLastName())
                .langKey(activatedUser.getLangKey())
                .authorities(Set.of("ROLE_USER"))
                .activated(activatedUser.isActivated())
                .build();
    }

    @Test
    void registerAccount() throws Exception {
        // given
        given(userService.registerUser(managedUserVM, managedUserVM.getPassword())).willReturn(createdUser);

        // when
        mockMvc.perform(post("/api/register")
                .content(objectMapper.writeValueAsString(managedUserVM))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated());

        // verify
        verify(userService, times(1)).registerUser(managedUserVM, managedUserVM.getPassword());
    }

    @Test
    void activateAccount() throws Exception {
        // given
        given(userService.activateRegistration(createdUser.getActivationKey())).willReturn(Optional.of(activatedUser));

        // when
        mockMvc.perform(get("/api/activate?key={activationKey}", createdUser.getActivationKey()))
                .andExpect(status().isOk());

        // verify
        verify(userService, times(1)).activateRegistration(createdUser.getActivationKey());
    }

    @Test
    void isAuthenticated() throws Exception {
        // given
        // when
        mockMvc.perform(
                        get("/api/authenticate")
                                .with(request -> {
                                    request.setRemoteUser(createdUser.getLogin());
                                    return request;
                                })
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(createdUser.getLogin()));

        // then
    }

    @Test
    void getAccount() throws Exception {
        // given
        given(userService.getUserWithAuthorities()).willReturn(Optional.of(activatedUser));
        given(userMapper.toAdminUserDTO(activatedUser)).willReturn(adminUserDTO);

        // when
        mockMvc
                .perform(get("/api/account").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.login").value(activatedUser.getLogin()))
                .andExpect(jsonPath("$.firstName").value(activatedUser.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(activatedUser.getLastName()))
                .andExpect(jsonPath("$.email").value(activatedUser.getEmail()))
                .andExpect(jsonPath("$.imageUrl").value(activatedUser.getImageUrl()))
                .andExpect(jsonPath("$.langKey").value(activatedUser.getLangKey()))
                .andExpect(jsonPath("$.authorities", containsInAnyOrder("ROLE_USER")));

        // then
        verify(userService, times(1)).getUserWithAuthorities();
    }

    @Test
    void saveAccount() {
        // given
        // when
        // verify
    }

    @Test
    void changePassword() throws Exception {
        // given
        PasswordChangeDTO passwordChangeDTO = PasswordChangeDTO.builder()
                .currentPassword(activatedUser.getPassword())
                .newPassword("new password")
                .build();
        // when
        mockMvc.perform(post("/api/account/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeDTO))
        ).andExpect(status().isOk());
        // then
    }

    @Test
    void requestPasswordReset() {
        // given
        // when
        // then
    }

    @Test
    void finishPasswordReset() throws Exception {
        // given
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();
        keyAndPasswordVM.setNewPassword("new password");
        keyAndPasswordVM.setKey(createdUser.getActivationKey());

        User resetPassword = User.builder()
                .id(createdUser.getId())
                .login(createdUser.getLogin())
                .email(createdUser.getEmail())
                .langKey(createdUser.getLangKey())
                .password(createdUser.getPassword())
                .authorities(Set.of(new Authority("ROLE_USER")))
                .resetKey(null)
                .resetDate(null)
                .password(keyAndPasswordVM.getNewPassword())
                .build();

        given(userService.completePasswordReset(keyAndPasswordVM.getNewPassword(), keyAndPasswordVM.getKey()))
                .willReturn(Optional.of(resetPassword));
        // when
        mockMvc.perform(post("/api/account/reset-password/finish?key={resetKey}", keyAndPasswordVM.getKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(keyAndPasswordVM))
        ).andExpect(status().isOk());
        // then
        verify(userService, times(1))
                .completePasswordReset(keyAndPasswordVM.getNewPassword(), keyAndPasswordVM.getKey());
    }
}