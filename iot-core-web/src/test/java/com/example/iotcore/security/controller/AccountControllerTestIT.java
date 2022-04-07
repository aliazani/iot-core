package com.example.iotcore.security.controller;

import com.example.iotcore.MySqlExtension;
import com.example.iotcore.security.AuthoritiesConstants;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTestIT extends MySqlExtension {
    private final static String PASSWORD_ADMIN_HASH = "$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC";
    private final ManagedUserVM managedUserVM = new ManagedUserVM();
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserService userService;
    @Autowired
    MailService mailService;
    private User ACTIVATED_USER;
    private User NON_ACTIVATED_USER;
    private User PASSWORD_RESET_USER;

    @BeforeEach
    void setUp() {
        managedUserVM.setLogin("new-user");
        managedUserVM.setEmail("new-user@email.com");
        managedUserVM.setLangKey("en");
        managedUserVM.setPassword("password");

        ACTIVATED_USER = User.builder()
                .id(2L)
                .login("user")
                .activated(true)
                .authorities(Set.of(new Authority("ROLE_USER")))
                .password("admin")
                .firstName("firstname-user")
                .lastName("lastname-user")
                .email("user@localhost.com")
                .langKey("en")
                .build();

        NON_ACTIVATED_USER = User.builder()
                .id(4L)
                .login("not-activated-user")
                .activationKey("activation-key")
                .activated(false)
                .authorities(Set.of(new Authority("ROLE_USER")))
                .password("admin")
                .resetKey("reset-key")
                .firstName("not-activated-firstname")
                .lastName("not-activated-lastname")
                .email("not-activated@localhost.com")
                .langKey("en")
                .build();

        NON_ACTIVATED_USER.setCreatedBy("anonymous");
        NON_ACTIVATED_USER.setCreatedDate(Instant.parse("2015-04-13T11:43:47.00Z"));


        PASSWORD_RESET_USER = User.builder()
                .id(5L)
                .login("password-reset")
                .resetKey("reset-key")
                .activated(true)
                .authorities(Set.of(new Authority("ROLE_USER")))
                .password("admin")
                .firstName("password-reset-firstname")
                .lastName("password-reset-lastname")
                .email("password-reset@localhost.com")
                .langKey("en")
                .build();
    }

    @Test
    @Transactional
    void registerAccount() throws Exception {
        // given

        // when
        mockMvc.perform(post("/api/register")
                .content(objectMapper.writeValueAsString(managedUserVM))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated());

        // verify
        User user = userRepository.findOneByLogin(managedUserVM.getLogin()).get();
        assertThat(user).isNotNull();
        assertThat(userRepository.findAll()).hasSize(6);
    }

    @Test
    @Transactional
    void activateAccount() throws Exception {
        // given

        // when
        mockMvc.perform(get("/api/activate?key={activationKey}", NON_ACTIVATED_USER.getActivationKey()))
                .andExpect(status().isOk());

        // verify
        User user = userRepository.findOneByLogin(NON_ACTIVATED_USER.getLogin()).get();
        assertThat(user.isActivated()).isTrue();
    }

    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    @Test
    void isAuthenticated() throws Exception {
        // given
        // when
        mockMvc.perform(
                        get("/api/authenticate")
                                .with(request -> {
                                    request.setRemoteUser(NON_ACTIVATED_USER.getLogin());
                                    return request;
                                })
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(NON_ACTIVATED_USER.getLogin()));

        // then
    }

    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    @Test
    void getAccount() throws Exception {
        // given

        // when
        mockMvc
                .perform(get("/api/account").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.login").value(NON_ACTIVATED_USER.getLogin()))
                .andExpect(jsonPath("$.firstName").value(NON_ACTIVATED_USER.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(NON_ACTIVATED_USER.getLastName()))
                .andExpect(jsonPath("$.email").value(NON_ACTIVATED_USER.getEmail()))
                .andExpect(jsonPath("$.imageUrl").value(NON_ACTIVATED_USER.getImageUrl()))
                .andExpect(jsonPath("$.langKey").value(NON_ACTIVATED_USER.getLangKey()))
                .andExpect(jsonPath("$.authorities", containsInAnyOrder("ROLE_USER")));

        // then
    }

    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    @Transactional
    @Test
    void saveAccount() throws Exception {
        // given
        AdminUserDTO adminUserDTO = AdminUserDTO.builder()
                .login(NON_ACTIVATED_USER.getLogin())
                .email(NON_ACTIVATED_USER.getEmail())
                .firstName("newFirstName-not-activated")
                .lastName(NON_ACTIVATED_USER.getLastName())
                .langKey(NON_ACTIVATED_USER.getLangKey())
                .imageUrl("http://new-image-url")
                .build();
        // when
        mockMvc.perform(post("/api/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminUserDTO))
        ).andExpect(status().isOk());
        // verify
        User user = userRepository.findOneByLogin(NON_ACTIVATED_USER.getLogin()).get();
        assertThat(user.getFirstName()).isEqualTo(adminUserDTO.getFirstName());
    }

    @Test
    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    @Transactional
    void changePassword() throws Exception {
        // given
        PasswordChangeDTO passwordChangeDTO = PasswordChangeDTO.builder()
                .currentPassword(NON_ACTIVATED_USER.getPassword())
                .newPassword("new password")
                .build();

        // when
        mockMvc.perform(post("/api/account/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordChangeDTO))
        ).andExpect(status().isOk());

        // then
        User user = userRepository.findOneByLogin(NON_ACTIVATED_USER.getLogin()).get();
        assertThat(user.getPassword()).isNotEqualTo(PASSWORD_ADMIN_HASH);
    }

    @Transactional
    @Test
    void requestPasswordReset() throws Exception {
        // given
        // when
        mockMvc.perform(post("/api/account/reset-password/init")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ACTIVATED_USER.getEmail())
        ).andExpect(status().isOk());
        // then
        User user = userRepository.findOneByLogin(ACTIVATED_USER.getLogin()).get();
        assertThat(user.getResetKey()).isNotEmpty();
    }

    @Transactional
    @Test
    void finishPasswordReset() throws Exception {
        // given
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();
        keyAndPasswordVM.setNewPassword("new password");
        keyAndPasswordVM.setKey(PASSWORD_RESET_USER.getResetKey());
        // when
        mockMvc.perform(post("/api/account/reset-password/finish?key={resetKey}",
                keyAndPasswordVM.getKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(keyAndPasswordVM))
        ).andExpect(status().isOk());
        // then
        User user = userRepository.findOneByLogin(PASSWORD_RESET_USER.getLogin()).get();
        assertThat(user.getResetKey()).isNull();
        assertThat(user.getPassword()).isNotEqualTo(PASSWORD_ADMIN_HASH);
    }
}