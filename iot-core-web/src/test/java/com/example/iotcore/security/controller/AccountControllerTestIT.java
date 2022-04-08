package com.example.iotcore.security.controller;

import com.example.iotcore.security.AuthoritiesConstants;
import com.example.iotcore.security.controller.vm.KeyAndPasswordVM;
import com.example.iotcore.security.controller.vm.ManagedUserVM;
import com.example.iotcore.security.domain.Authority;
import com.example.iotcore.security.domain.User;
import com.example.iotcore.security.dto.AdminUserDTO;
import com.example.iotcore.security.dto.PasswordChangeDTO;
import com.example.iotcore.security.mapper.UserMapper;
import com.example.iotcore.security.repository.AuthorityRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.thymeleaf.util.StringUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Testcontainers
class AccountControllerTestIT {
    private final static String PASSWORD_ADMIN_HASH = "$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC";
    @Container
    private static final MySQLContainer MY_SQL_CONTAINER = (MySQLContainer) new MySQLContainer("mysql:8.0.28")
            .withExposedPorts(3306);
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
    AuthorityRepository authorityRepository;
    @Autowired
    MailService mailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    private User ACTIVATED_USER;
    private User NON_ACTIVATED_USER;
    private User PASSWORD_RESET_USER;

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", MY_SQL_CONTAINER::getDriverClassName);
    }

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
    void registerAccount() throws Exception {
        // given

        // when
        mockMvc.perform(post("/api/register")
                .content(objectMapper.writeValueAsString(managedUserVM))
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isCreated());

        // then
        User user = userRepository.findOneByLogin(managedUserVM.getLogin()).get();
        assertThat(user).isNotNull();
        assertThat(userRepository.findAll()).hasSize(6);
    }

    @Test
    void registerAccount_invalidLogin() throws Exception {
        // given
        ManagedUserVM invalidLogin = new ManagedUserVM();
        invalidLogin.setLogin("invalid-login(s");
        invalidLogin.setEmail("invalid-login@email.com");
        invalidLogin.setLangKey("en");
        invalidLogin.setPassword("password");

        // when
        mockMvc.perform(post("/api/register")
                        .content(objectMapper.writeValueAsString(invalidLogin))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.validation"))
                .andExpect(jsonPath("$.fieldErrors.[0].field").value("login"))
        ;

        // then
        Optional<User> user = userRepository.findOneByEmailIgnoreCase(invalidLogin.getEmail());
        assertThat(user).isEmpty();
    }

    @Test
    void registerAccount_invalidEmail() throws Exception {
        // given
        ManagedUserVM invalidEmail = new ManagedUserVM();
        invalidEmail.setLogin("invalid-email");
        invalidEmail.setEmail("invalid-email@");
        invalidEmail.setLangKey("en");
        invalidEmail.setPassword("password");

        // when
        mockMvc.perform(post("/api/register")
                        .content(objectMapper.writeValueAsString(invalidEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.validation"))
                .andExpect(jsonPath("$.fieldErrors.[0].field").value("email"))
        ;

        // then
        Optional<User> user = userRepository.findOneByLogin(invalidEmail.getLogin());
        assertThat(user).isEmpty();
    }

    @Test
    void registerAccount_invalidPassword() throws Exception {
        // given
        ManagedUserVM invalidPassword = new ManagedUserVM();
        invalidPassword.setLogin("invalid-password");
        invalidPassword.setEmail("invalid-password@email.com");
        invalidPassword.setLangKey("en");
        invalidPassword.setPassword("123");

        // when
        mockMvc.perform(post("/api/register")
                        .content(objectMapper.writeValueAsString(invalidPassword))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.validation"))
                .andExpect(jsonPath("$.fieldErrors.[0].field").value("password"))
        ;

        // then
        Optional<User> user = userRepository.findOneByEmailIgnoreCase(invalidPassword.getEmail());
        assertThat(user).isEmpty();
    }

    @Test
    void registerAccount_nullPassword() throws Exception {
        // given
        ManagedUserVM nullPassword = new ManagedUserVM();
        nullPassword.setLogin("null-password");
        nullPassword.setEmail("null-password@email.com");
        nullPassword.setLangKey("en");
        nullPassword.setPassword(null);

        // when
        mockMvc.perform(post("/api/register")
                        .content(objectMapper.writeValueAsString(nullPassword))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Incorrect password"))
        ;

        // then
        Optional<User> user = userRepository.findOneByEmailIgnoreCase(nullPassword.getEmail());
        assertThat(user).isEmpty();
    }

    @Test
    void registerAccount_duplicateLogin() throws Exception {
        // given
        ManagedUserVM duplicateLogin = new ManagedUserVM();
        duplicateLogin.setLogin(ACTIVATED_USER.getLogin());
        duplicateLogin.setEmail("duplicateLogin@email.com");
        duplicateLogin.setLangKey("en");
        duplicateLogin.setPassword("password");

        // when
        mockMvc.perform(post("/api/register")
                        .content(objectMapper.writeValueAsString(duplicateLogin))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Login name already used!"))
                .andExpect(jsonPath("$.message").value("error.userexists"))
        ;

        // then
        Optional<User> user = userRepository.findOneByEmailIgnoreCase(duplicateLogin.getEmail());
        assertThat(user).isEmpty();
    }

    @Test
    void registerAccount_duplicateEmail() throws Exception {
        // given
        ManagedUserVM duplicateEmail = new ManagedUserVM();
        duplicateEmail.setLogin("duplicate-email");
        duplicateEmail.setEmail(ACTIVATED_USER.getEmail());
        duplicateEmail.setLangKey("en");
        duplicateEmail.setPassword("password");

        // when
        mockMvc.perform(post("/api/register")
                        .content(objectMapper.writeValueAsString(duplicateEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Email is already in use!"))
                .andExpect(jsonPath("$.message").value("error.emailexists"))
        ;

        // then
        Optional<User> user = userRepository.findOneByLogin(duplicateEmail.getLogin());
        assertThat(user).isEmpty();
    }

    @Test
    void registerAccount_adminIsIgnored() throws Exception {
        // given
        ManagedUserVM adminIsIgnored = new ManagedUserVM();
        adminIsIgnored.setLogin("admin-is-ignored");
        adminIsIgnored.setEmail("admin-is-ignored@email.com");
        adminIsIgnored.setLangKey("en");
        adminIsIgnored.setPassword("password");
        adminIsIgnored.setAuthorities(Set.of("ROLE_ADMIN"));

        // when
        mockMvc.perform(post("/api/register")
                        .content(objectMapper.writeValueAsString(adminIsIgnored))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated());

        // then
        Optional<User> user = userRepository.findOneByEmailIgnoreCase(adminIsIgnored.getEmail());
        assertThat(user).isPresent();
        assertThat(user.get().getAuthorities()).hasSize(1)
                .containsExactly(authorityRepository.findById(AuthoritiesConstants.USER).get());
    }

    @Test
    void activateAccount() throws Exception {
        // given

        // when
        mockMvc.perform(get("/api/activate?key={activationKey}", NON_ACTIVATED_USER.getActivationKey()))
                .andExpect(status().isOk());

        // then
        User user = userRepository.findOneByLogin(NON_ACTIVATED_USER.getLogin()).get();
        assertThat(user.isActivated()).isTrue();
    }

    @Test
    void activateAccount_wrongActivationKey() throws Exception {
        // given

        // when
        mockMvc.perform(get("/api/activate?key=wrongActivationKey")
        ).andExpect(status().isInternalServerError());

        // then
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

    @Test
    void isAuthenticated_nonAuthenticated() throws Exception {
        // given
        // when
        mockMvc.perform(
                        get("/api/authenticate").accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(""));

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

    @WithMockUser(username = "test", password = "admin", authorities = AuthoritiesConstants.USER)
    @Test
    void getAccount_unknownAccount() throws Exception {
        // given

        // when
        mockMvc
                .perform(get("/api/account").accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isInternalServerError());

        // then
    }

    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
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
        // then
        User user = userRepository.findOneByLogin(NON_ACTIVATED_USER.getLogin()).get();
        assertThat(user.getFirstName()).isEqualTo(adminUserDTO.getFirstName());
    }

    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    @Test
    void saveAccount_invalidEmail() throws Exception {
        // given
        AdminUserDTO invalidEmail = AdminUserDTO.builder()
                .login(NON_ACTIVATED_USER.getLogin())
                .email("invalid email")
                .firstName("newFirstName-not-activated")
                .lastName(NON_ACTIVATED_USER.getLastName())
                .langKey(NON_ACTIVATED_USER.getLangKey())
                .imageUrl("http://new-image-url")
                .build();
        // when
        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmail))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.validation"))
                .andExpect(jsonPath("$.fieldErrors.[0].field").value("email"))
        ;
        // then
        User user = userRepository.findOneByLogin(invalidEmail.getLogin()).get();
        assertThat(user.getEmail()).isNotEqualTo(invalidEmail.getEmail());
    }

    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    @Test
    void saveAccount_invalidLogin() throws Exception {
        // given
        AdminUserDTO invalidLogin = AdminUserDTO.builder()
                .login("invalid login(")
                .email(NON_ACTIVATED_USER.getEmail())
                .firstName("newFirstName-not-activated")
                .lastName(NON_ACTIVATED_USER.getLastName())
                .langKey(NON_ACTIVATED_USER.getLangKey())
                .imageUrl("http://new-image-url")
                .build();
        // when
        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("error.validation"))
                .andExpect(jsonPath("$.fieldErrors.[0].field").value("login"))
        ;
        // then
        User user = userRepository.findOneByEmailIgnoreCase(invalidLogin.getEmail()).get();
        assertThat(user.getLogin()).isNotEqualTo(invalidLogin.getLogin());
    }

    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    @Test
    void saveAccount_existingEmail() throws Exception {
        // given
        AdminUserDTO duplicateEmail = AdminUserDTO.builder()
                .login(NON_ACTIVATED_USER.getLogin())
                .email(ACTIVATED_USER.getEmail())
                .firstName("newFirstName-not-activated")
                .lastName(NON_ACTIVATED_USER.getLastName())
                .langKey(NON_ACTIVATED_USER.getLangKey())
                .imageUrl("http://new-image-url")
                .build();
        // when
        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateEmail))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Email is already in use!"))
                .andExpect(jsonPath("$.message").value("error.emailexists"))
        ;
        // then
        User user = userRepository.findOneByLogin(duplicateEmail.getLogin()).get();
        assertThat(user.getEmail()).isNotEqualTo(duplicateEmail.getEmail());
    }

    @WithMockUser(username = "unknown", password = "admin", authorities = AuthoritiesConstants.USER)
    @Test
    void saveAccount_nonExistingUser() throws Exception {
        // given
        AdminUserDTO unknownUser = AdminUserDTO.builder()
                .login("unknown")
                .email("unknown@email.com")
                .firstName("unknown-firstname")
                .lastName("unknown-lastname")
                .langKey("en")
                .imageUrl("http://new-image-url")
                .build();
        // when
        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownUser))
                )
                .andExpect(status().isInternalServerError())
        ;
        // then
        Optional<User> user = userRepository.findOneByLogin(unknownUser.getLogin());
        assertThat(user).isNotPresent();
    }

    @Test
    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
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

    @Test
    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    void changePassword_wrongExistingPassword() throws Exception {
        // given
        PasswordChangeDTO existingPassword = PasswordChangeDTO.builder()
                .currentPassword("1" + NON_ACTIVATED_USER.getPassword())
                .newPassword("new password")
                .build();

        // when
        mockMvc.perform(post("/api/account/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingPassword))
                )
                .andExpect(status().isBadRequest());

        // then
        User updatedUser = userRepository.findOneByLogin(NON_ACTIVATED_USER.getLogin()).get();
        assertThat(passwordEncoder.matches(existingPassword.getNewPassword(),
                updatedUser.getPassword())).isFalse();
        assertThat(updatedUser.getPassword()).isEqualTo(PASSWORD_ADMIN_HASH);
    }

    @Test
    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    void changePassword_passwordTooSmall() throws Exception {
        // given
        PasswordChangeDTO tooSmallPassword = PasswordChangeDTO.builder()
                .currentPassword(NON_ACTIVATED_USER.getPassword())
                .newPassword("123")
                .build();

        // when
        mockMvc.perform(post("/api/account/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tooSmallPassword))
        ).andExpect(status().isBadRequest());

        // then
        User updatedUser = userRepository.findOneByLogin(NON_ACTIVATED_USER.getLogin()).get();
        assertThat(passwordEncoder.matches(tooSmallPassword.getNewPassword(),
                updatedUser.getPassword())).isFalse();
        assertThat(updatedUser.getPassword()).isEqualTo(PASSWORD_ADMIN_HASH);
    }

    @Test
    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    void changePassword_passwordTooLong() throws Exception {
        // given
        PasswordChangeDTO tooLongPassword = PasswordChangeDTO.builder()
                .currentPassword(NON_ACTIVATED_USER.getPassword())
                .newPassword(StringUtils.randomAlphanumeric(ManagedUserVM.PASSWORD_MAX_LENGTH + 1))
                .build();

        // when
        mockMvc.perform(post("/api/account/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tooLongPassword))
        ).andExpect(status().isBadRequest());

        // then
        User updatedUser = userRepository.findOneByLogin(NON_ACTIVATED_USER.getLogin()).get();
        assertThat(passwordEncoder.matches(tooLongPassword.getNewPassword(),
                updatedUser.getPassword())).isFalse();
        assertThat(updatedUser.getPassword()).isEqualTo(PASSWORD_ADMIN_HASH);
    }

    @Test
    @WithMockUser(username = "not-activated-user", password = "admin", authorities = AuthoritiesConstants.USER)
    void changePassword_emptyPassword() throws Exception {
        // given
        PasswordChangeDTO tooLongPassword = PasswordChangeDTO.builder()
                .currentPassword(NON_ACTIVATED_USER.getPassword())
                .newPassword("")
                .build();

        // when
        mockMvc.perform(post("/api/account/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tooLongPassword))
        ).andExpect(status().isBadRequest());

        // then
        User updatedUser = userRepository.findOneByLogin(NON_ACTIVATED_USER.getLogin()).get();
        assertThat(passwordEncoder.matches(tooLongPassword.getNewPassword(),
                updatedUser.getPassword())).isFalse();
        assertThat(updatedUser.getPassword()).isEqualTo(PASSWORD_ADMIN_HASH);
    }

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

    @Test
    void requestPasswordReset_upperCaseEmail() throws Exception {
        // given
        // when
        mockMvc.perform(post("/api/account/reset-password/init")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ACTIVATED_USER.getEmail().toUpperCase())
        ).andExpect(status().isOk());
        // then
        User user = userRepository.findOneByLogin(ACTIVATED_USER.getLogin()).get();
        assertThat(user.getResetKey()).isNotEmpty();
    }

    @Test
    void requestPasswordReset_wrongEmail() throws Exception {
        // given
        // when
        mockMvc.perform(post("/api/account/reset-password/init")
                .contentType(MediaType.APPLICATION_JSON)
                .content("wrongEmail@email.com")
        ).andExpect(status().isOk());
        // then
        Optional<User> user = userRepository.findOneByEmailIgnoreCase("wrongEmail@email.com");
        assertThat(user).isNotPresent();
    }

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

    @Test
    void finishPasswordReset_passwordTooSmall() throws Exception {
        // given
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();
        keyAndPasswordVM.setNewPassword("123");
        keyAndPasswordVM.setKey(PASSWORD_RESET_USER.getResetKey());
        // when
        mockMvc.perform(post("/api/account/reset-password/finish?key={resetKey}",
                keyAndPasswordVM.getKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(keyAndPasswordVM))
        ).andExpect(status().isBadRequest());
        // then
        User user = userRepository.findOneByLogin(PASSWORD_RESET_USER.getLogin()).get();
        assertThat(user.getPassword()).isEqualTo(PASSWORD_ADMIN_HASH);
        assertThat(user.getResetKey()).isNotNull();
    }

    @Test
    void finishPasswordReset_wrongKey() throws Exception {
        // given
        KeyAndPasswordVM keyAndPasswordVM = new KeyAndPasswordVM();
        keyAndPasswordVM.setNewPassword("new password");
        keyAndPasswordVM.setKey("wrong key");
        // when
        mockMvc.perform(post("/api/account/reset-password/finish?key={resetKey}",
                        keyAndPasswordVM.getKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(keyAndPasswordVM))
                )
                .andExpect(status().isInternalServerError());
        // then
    }
}