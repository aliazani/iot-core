package com.example.iotcore.security.controller;

import com.example.iotcore.security.AuthoritiesConstants;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Transactional
class UserControllerTestIT {
    private static final String ENTITY_API_URL = "/api/admin/users";
    private static final String ENTITY_API_URL_LOGIN = ENTITY_API_URL + "/{login}";
    @Container
    private static final MySQLContainer MY_SQL_CONTAINER = (MySQLContainer) new MySQLContainer("mysql:8.0.28")
            .withExposedPorts(3306);
    private static final String ENTITY_NAME = "userManagement";
    private static User USER_1;
    private static User USER_2;
    private static User USER_3;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserMapper userMapper;
    @Autowired
    MailService mailService;
    private AdminUserDTO adminUserDTO6;
    private User user6;
    @Value("${application.clientApp.name}")
    private String applicationName;

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", MY_SQL_CONTAINER::getDriverClassName);
    }

    @BeforeEach
    void setUp() {
        USER_1 = User.builder()
                .id(1L)
                .login("admin")
                .password("admin")
                .firstName("Administrator")
                .lastName("Administrator")
                .email("admin@localhost")
                .activated(true)
                .langKey("en")
                .authorities(Set.of(Authority.builder().name("ROLE_ADMIN").build()))
                .build();
        USER_1.setCreatedBy("system");
        USER_1.setLastModifiedBy("system");

        USER_2 = User.builder()
                .id(2L)
                .login("user")
                .password("admin")
                .firstName("firstname-user")
                .lastName("lastname-user")
                .email("user@localhost.com")
                .activated(true)
                .langKey("en")
                .authorities(Set.of(Authority.builder().name("ROLE_USER").build()))
                .build();
        USER_2.setCreatedBy("system");
        USER_2.setLastModifiedBy("system");

        USER_3 = User.builder()
                .id(3L)
                .login("user-admin")
                .password("admin")
                .firstName("firstname-admin-user")
                .lastName("lastname-admin-user")
                .email("user_admin@localhost.com")
                .activated(true)
                .langKey("en")
                .authorities(Set.of(Authority.builder().name("ROLE_ADMIN").build(),
                        Authority.builder().name("ROLE_USER").build()))
                .build();
        USER_3.setCreatedBy("system");
        USER_3.setLastModifiedBy("system");

        adminUserDTO6 = AdminUserDTO.builder()
                .id(6L)
                .login("user6")
                .firstName("firstname-user6")
                .lastName("lastname-user-6")
                .email("user5@localhost.com")
                .imageUrl("http://placehold.it/50x50")
                .langKey("EN")
                .authorities(Set.of("ROLE_USER"))
                .build();

        user6 = User.builder()
                .id(adminUserDTO6.getId())
                .login(adminUserDTO6.getLogin())
                .firstName(adminUserDTO6.getFirstName())
                .lastName(adminUserDTO6.getLastName())
                .email(adminUserDTO6.getEmail())
                .imageUrl(adminUserDTO6.getImageUrl())
                .langKey(adminUserDTO6.getLangKey())
                .activated(true)
                .authorities(Set.of(new Authority("ROLE_USER")))
                .password("password")
                .resetKey("resetKey")
                .resetDate(Instant.now())
                .build();
    }

    @Test
    void createUser() throws Exception {
        // given
        AdminUserDTO adminUserDTO = AdminUserDTO.builder()
                .login(adminUserDTO6.getLogin())
                .firstName(adminUserDTO6.getFirstName())
                .lastName(adminUserDTO6.getLastName())
                .email(adminUserDTO6.getEmail())
                .imageUrl(adminUserDTO6.getImageUrl())
                .langKey(adminUserDTO6.getLangKey())
                .authorities(adminUserDTO6.getAuthorities())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUserDTO))
                )
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(user6.getId().intValue()))
                .andExpect(jsonPath("$.login").value(user6.getLogin()))
                .andExpect(jsonPath("$.firstName").value(user6.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(user6.getLastName()))
                .andExpect(jsonPath("$.email").value(user6.getEmail()))
                .andExpect(jsonPath("$.imageUrl").value(user6.getImageUrl()))
                .andExpect(jsonPath("$.langKey").value(user6.getLangKey()))
                .andExpect(jsonPath("$.activated").value(user6.isActivated()))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.created".formatted(ENTITY_NAME)));

        // then
        assertThat(userRepository.existsById(user6.getId())).isTrue();
        assertThat(userRepository.findAll()).hasSize(6);
    }

    @Test
    void createUser_existingId() throws Exception {
        // given
        AdminUserDTO adminUserDTO = AdminUserDTO.builder()
                .id(adminUserDTO6.getId())
                .login(adminUserDTO6.getLogin())
                .firstName(adminUserDTO6.getFirstName())
                .lastName(adminUserDTO6.getLastName())
                .email(adminUserDTO6.getEmail())
                .imageUrl(adminUserDTO6.getImageUrl())
                .langKey(adminUserDTO6.getLangKey())
                .authorities(adminUserDTO6.getAuthorities())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUserDTO))
                )
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value("userManagement"))
                .andExpect(jsonPath("$.errorKey").value("idexists"))
        ;

        // then
        assertThat(userRepository.existsById(user6.getId())).isFalse();
        assertThat(userRepository.findAll()).hasSize(5);
    }

    @Test
    void createUser_existingLogin() throws Exception {
        // given
        AdminUserDTO adminUserDTO = AdminUserDTO.builder()
                .login(USER_1.getLogin())
                .firstName(adminUserDTO6.getFirstName())
                .lastName(adminUserDTO6.getLastName())
                .email(adminUserDTO6.getEmail())
                .imageUrl(adminUserDTO6.getImageUrl())
                .langKey(adminUserDTO6.getLangKey())
                .authorities(adminUserDTO6.getAuthorities())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUserDTO))
                )
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value("userManagement"))
                .andExpect(jsonPath("$.errorKey").value("userexists"))
        ;

        // then
        assertThat(userRepository.existsById(user6.getId())).isFalse();
        assertThat(userRepository.findAll()).hasSize(5);
    }

    @Test
    void createUser_existingEmail() throws Exception {
        // given
        AdminUserDTO adminUserDTO = AdminUserDTO.builder()
                .login(adminUserDTO6.getLogin())
                .firstName(adminUserDTO6.getFirstName())
                .lastName(adminUserDTO6.getLastName())
                .email(USER_1.getEmail())
                .imageUrl(adminUserDTO6.getImageUrl())
                .langKey(adminUserDTO6.getLangKey())
                .authorities(adminUserDTO6.getAuthorities())
                .build();

        // when
        mockMvc.perform(post(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminUserDTO))
                )
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value("userManagement"))
                .andExpect(jsonPath("$.errorKey").value("emailexists"))
        ;

        // then
        assertThat(userRepository.existsById(user6.getId())).isFalse();
        assertThat(userRepository.findAll()).hasSize(5);
    }

    @Test
    void updateUser() throws Exception {
        // given
        AdminUserDTO updatedAdminUserDTO = AdminUserDTO.builder()
                .id(USER_1.getId())
                .login(USER_1.getLogin())
                .firstName("updatedFirstName")
                .lastName("updatedLastName")
                .email(USER_1.getEmail())
                .imageUrl(USER_1.getImageUrl())
                .activated(true)
                .langKey(USER_1.getLangKey())
                .authorities(Set.of("ROLE_USER"))
                .build();

        // when
        mockMvc.perform(put(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAdminUserDTO))
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(updatedAdminUserDTO.getId().intValue()))
                .andExpect(jsonPath("$.login").value(updatedAdminUserDTO.getLogin()))
                .andExpect(jsonPath("$.firstName").value(updatedAdminUserDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(updatedAdminUserDTO.getLastName()))
                .andExpect(jsonPath("$.email").value(updatedAdminUserDTO.getEmail()))
                .andExpect(jsonPath("$.imageUrl").value(updatedAdminUserDTO.getImageUrl()))
                .andExpect(jsonPath("$.langKey").value(updatedAdminUserDTO.getLangKey()))
                .andExpect(jsonPath("$.activated").value(updatedAdminUserDTO.isActivated()))
                .andExpect(jsonPath("$.authorities", containsInAnyOrder("ROLE_USER")))
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.updated".formatted(ENTITY_NAME)));

        // then
        User user = userRepository.findById(USER_1.getId()).get();
        assertThat(user.getFirstName()).isEqualTo(updatedAdminUserDTO.getFirstName());
        assertThat(user.getLastName()).isEqualTo(updatedAdminUserDTO.getLastName());
        assertThat(user.getAuthorities()).isEqualTo(user6.getAuthorities());
    }

    @Test
    void updateUser_existingEmail() throws Exception {
        // given
        AdminUserDTO updatedAdminUserDTO = AdminUserDTO.builder()
                .id(USER_1.getId())
                .login(USER_1.getLogin())
                .firstName("updatedFirstName")
                .lastName("updatedLastName")
                .email(USER_2.getEmail())
                .imageUrl(USER_1.getImageUrl())
                .activated(true)
                .langKey(USER_1.getLangKey())
                .authorities(Set.of("ROLE_USER"))
                .build();

        // when
        mockMvc.perform(put(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAdminUserDTO))
                )
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value("userManagement"))
                .andExpect(jsonPath("$.errorKey").value("emailexists"))
        ;

        // then
        User user = userRepository.findById(USER_1.getId()).get();
        assertThat(user.getFirstName()).isEqualTo(USER_1.getFirstName());
        assertThat(user.getLastName()).isEqualTo(USER_1.getLastName());
        assertThat(user.getFirstName()).isNotEqualTo(updatedAdminUserDTO.getFirstName());
        assertThat(user.getLastName()).isNotEqualTo(updatedAdminUserDTO.getLastName());
        assertThat(user.getAuthorities()).isNotEqualTo(user6.getAuthorities());
    }

    @Test
    void updateUser_existingLogin() throws Exception {
        // given
        AdminUserDTO updatedAdminUserDTO = AdminUserDTO.builder()
                .id(USER_1.getId())
                .login(USER_2.getLogin())
                .firstName("updatedFirstName")
                .lastName("updatedLastName")
                .email(USER_1.getEmail())
                .imageUrl(USER_1.getImageUrl())
                .activated(true)
                .langKey(USER_1.getLangKey())
                .authorities(Set.of("ROLE_USER"))
                .build();

        // when
        mockMvc.perform(put(ENTITY_API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAdminUserDTO))
                )
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.entityName").value("userManagement"))
                .andExpect(jsonPath("$.errorKey").value("userexists"))
        ;

        // then
        User user = userRepository.findById(USER_1.getId()).get();
        assertThat(user.getFirstName()).isEqualTo(USER_1.getFirstName());
        assertThat(user.getLastName()).isEqualTo(USER_1.getLastName());
        assertThat(user.getFirstName()).isNotEqualTo(updatedAdminUserDTO.getFirstName());
        assertThat(user.getLastName()).isNotEqualTo(updatedAdminUserDTO.getLastName());
        assertThat(user.getAuthorities()).isNotEqualTo(user6.getAuthorities());
    }

    @Test
    void getAllUsers() throws Exception {
        // given
        // when
        mockMvc.perform(get(ENTITY_API_URL + "?sort=id,asc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.[0].id").value(USER_1.getId().intValue()))
                .andExpect(jsonPath("$.[0].login").value(USER_1.getLogin()))
                .andExpect(jsonPath("$.[0].firstName").value(USER_1.getFirstName()))
                .andExpect(jsonPath("$.[0].lastName").value(USER_1.getLastName()))
                .andExpect(jsonPath("$.[0].email").value(USER_1.getEmail()))
                .andExpect(jsonPath("$.[0].imageUrl").value(USER_1.getImageUrl()))
                .andExpect(jsonPath("$.[0].langKey").value(USER_1.getLangKey()))
                .andExpect(jsonPath("$.[0].activated").value(USER_1.isActivated()))
                .andExpect(jsonPath("$.[0].authorities", containsInAnyOrder("ROLE_ADMIN")))

                .andExpect(jsonPath("$.[1].id").value(USER_2.getId().intValue()))
                .andExpect(jsonPath("$.[1].login").value(USER_2.getLogin()))
                .andExpect(jsonPath("$.[1].firstName").value(USER_2.getFirstName()))
                .andExpect(jsonPath("$.[1].lastName").value(USER_2.getLastName()))
                .andExpect(jsonPath("$.[1].email").value(USER_2.getEmail()))
                .andExpect(jsonPath("$.[1].imageUrl").value(USER_2.getImageUrl()))
                .andExpect(jsonPath("$.[1].langKey").value(USER_2.getLangKey()))
                .andExpect(jsonPath("$.[1].activated").value(USER_2.isActivated()))
                .andExpect(jsonPath("$.[1].authorities", containsInAnyOrder("ROLE_USER")))

                .andExpect(jsonPath("$.[2].id").value(USER_3.getId().intValue()))
                .andExpect(jsonPath("$.[2].login").value(USER_3.getLogin()))
                .andExpect(jsonPath("$.[2].firstName").value(USER_3.getFirstName()))
                .andExpect(jsonPath("$.[2].lastName").value(USER_3.getLastName()))
                .andExpect(jsonPath("$.[2].email").value(USER_3.getEmail()))
                .andExpect(jsonPath("$.[2].imageUrl").value(USER_3.getImageUrl()))
                .andExpect(jsonPath("$.[2].langKey").value(USER_3.getLangKey()))
                .andExpect(jsonPath("$.[2].activated").value(USER_3.isActivated()))
                .andExpect(jsonPath("$.[2].authorities", containsInAnyOrder("ROLE_USER", "ROLE_ADMIN")))
        ;

        mockMvc.perform(get(ENTITY_API_URL + "?sort=authorities,desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        // then
    }

    @Test
    void getUser() throws Exception {
        // given

        // when
        mockMvc.perform(get(ENTITY_API_URL_LOGIN, USER_1.getLogin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(USER_1.getId().intValue()))
                .andExpect(jsonPath("$.login").value(USER_1.getLogin()))
                .andExpect(jsonPath("$.firstName").value(USER_1.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(USER_1.getLastName()))
                .andExpect(jsonPath("$.email").value(USER_1.getEmail()))
                .andExpect(jsonPath("$.imageUrl").value(USER_1.getImageUrl()))
                .andExpect(jsonPath("$.langKey").value(USER_1.getLangKey()))
                .andExpect(jsonPath("$.activated").value(USER_1.isActivated()))
                .andExpect(jsonPath("$.authorities", containsInAnyOrder("ROLE_ADMIN")));

        // then
    }

    @Test
    void getUser_nonExistingUser() throws Exception {
        // given

        // when
        mockMvc.perform(get(ENTITY_API_URL_LOGIN, adminUserDTO6.getLogin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.path").value(ENTITY_API_URL + "/" + adminUserDTO6.getLogin()))
        ;

        // then
    }

    @Test
    void deleteUser() throws Exception {
        // given
        // when
        mockMvc.perform(delete(ENTITY_API_URL_LOGIN, USER_3.getLogin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent())
                .andExpect(header().string("X-" + applicationName + "-alert",
                        "%s.deleted".formatted(ENTITY_NAME)));

        // then
        assertThat(userRepository.findById(USER_3.getId())).isEmpty();
        assertThat(userRepository.findAll()).hasSize(4);
    }
}