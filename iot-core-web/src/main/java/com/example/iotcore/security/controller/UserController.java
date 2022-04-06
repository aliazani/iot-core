package com.example.iotcore.security.controller;

import com.example.iotcore.config.Constants;
import com.example.iotcore.security.AuthoritiesConstants;
import com.example.iotcore.security.domain.User;
import com.example.iotcore.security.dto.AdminUserDTO;
import com.example.iotcore.security.mapper.UserMapper;
import com.example.iotcore.security.repository.UserRepository;
import com.example.iotcore.security.service.MailService;
import com.example.iotcore.security.service.UserService;
import com.example.iotcore.util.HeaderUtil;
import com.example.iotcore.util.PaginationUtil;
import com.example.iotcore.util.ResponseUtil;
import com.example.iotcore.web.errors.BadRequestAlertException;
import com.example.iotcore.web.errors.EmailAlreadyUsedException;
import com.example.iotcore.web.errors.LoginAlreadyUsedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the {@link User} entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@Tag(name = "User Management", description = "User Management ONLY FOR ADMIN")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin")
public class UserController {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList(
                    "id",
                    "login",
                    "firstName",
                    "lastName",
                    "email",
                    "activated",
                    "langKey",
                    "createdBy",
                    "createdDate",
                    "lastModifiedBy",
                    "lastModifiedDate"
            )
    );
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MailService mailService;
    @Value("${application.clientApp.name}")
    private String applicationName;

    /**
     * {@code POST  /admin/users}  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends a
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param adminUserDTO the user to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user, or with status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws URISyntaxException        if the Location URI syntax is incorrect.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already in use.
     */
    @Operation(summary = "Create new user", description = "Create new user",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AdminUserDTO.class))}
                            , headers = @Header(name = "Location", description = "The URL of the created user")
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (login/email already in use)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Authentication Failure(Only admin can access this API)",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(examples = {@ExampleObject(name = "AdminUserDTO",
                    value = "{\"login\": \"string\", \"email\": \"string@email.com\"," +
                            "\"langKey\": \"string\", \"firstName\": \"string\", \"lastName\": \"string\"," +
                            "\"activated\": true, \"imageUrl\": \"string\"," +
                            "  \"authorities\": [\"ROLE_USER\" ,\"ROLE_ADMIN\"]" +
                            "}")}))
    @PostMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<User> createUser(@Valid @RequestBody AdminUserDTO adminUserDTO) throws URISyntaxException {
        log.debug("REST request to save User : {}", adminUserDTO);

        // Lowercase the user login before comparing with database
        if (adminUserDTO.getId() != null)
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
        else if (userRepository.findOneByLogin(adminUserDTO.getLogin().toLowerCase()).isPresent())
            throw new LoginAlreadyUsedException();
        else if (userRepository.findOneByEmailIgnoreCase(adminUserDTO.getEmail()).isPresent())
            throw new EmailAlreadyUsedException();
        else {
            User newUser = userService.createUser(adminUserDTO);
            mailService.sendCreationEmail(newUser);

            return ResponseEntity
                    .created(new URI("/api/admin/users/" + newUser.getLogin()))
                    .headers(HeaderUtil.createAlert(applicationName, "userManagement.created", newUser.getLogin()))
                    .body(newUser);
        }
    }

    /**
     * {@code PUT /admin/users} : Updates an existing User.
     *
     * @param adminUserDTO the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already in use.
     */
    @Operation(summary = "Update user", description = "Update user",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "User updated successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AdminUserDTO.class))}
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (login/email is already in use)",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "401",
                            description = "Authentication Failure(Only admin can access this API)",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(examples = {@ExampleObject(name = "AdminUserDTO",
                    value = "{\"id\": 1, \"login\": \"string\", \"email\": \"string@email.com\"," +
                            "\"langKey\": \"string\", \"firstName\": \"string\", \"lastName\": \"string\"," +
                            "\"activated\": true, \"imageUrl\": \"string\"," +
                            "  \"authorities\": [\"ROLE_USER\" ,\"ROLE_ADMIN\"]" +
                            "}")}))
    @PutMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> updateUser(@Valid @RequestBody AdminUserDTO adminUserDTO) {
        log.debug("REST request to update User : {}", adminUserDTO);

        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(adminUserDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(adminUserDTO.getId())))
            throw new EmailAlreadyUsedException();
        existingUser = userRepository.findOneByLogin(adminUserDTO.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(adminUserDTO.getId())))
            throw new LoginAlreadyUsedException();

        Optional<AdminUserDTO> updatedUser = userService.updateUser(adminUserDTO);

        return ResponseUtil.wrapOrNotFound(
                updatedUser,
                HeaderUtil.createAlert(applicationName, "userManagement.updated", adminUserDTO.getLogin())
        );
    }

    /**
     * {@code GET /admin/users} : get all users with all the details - calling this are only allowed for the administrators.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */

    @Operation(summary = "Get all users paged", description = "Get all users paged",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved a page of users",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AdminUserDTO.class))}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/users")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all User for an admin");

        if (!onlyContainsAllowedProperties(pageable))
            return ResponseEntity.badRequest().build();

        final Page<AdminUserDTO> page = userService.getAllManagedUsers(pageable);
        HttpHeaders headers =
                PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable
                .getSort()
                .stream()
                .map(Sort.Order::getProperty)
                .allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    /**
     * {@code GET /admin/users/:login} : get the "login" user.
     *
     * @param login the login of the user to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" user, or with status {@code 404 (Not Found)}.
     */

    @Operation(summary = "Find a username(login) by ID", description = "Find a username(login) by ID",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "successfully retrieved a username(login)",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AdminUserDTO.class))}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "User Not found",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/users/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AdminUserDTO> getUser(@PathVariable @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        log.debug("REST request to get User : {}", login);

        return ResponseUtil
                .wrapOrNotFound(userService.getUserWithAuthoritiesByLogin(login)
                        .map(userMapper::toAdminUserDTO));
    }

    /**
     * {@code DELETE /admin/users/:login} : delete the "login" User.
     *
     * @param login the login of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */

    @Operation(summary = "Delete a user", description = "Delete a user by username(login)",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "204", description = "User successfully deleted",
                            content = {@Content(mediaType = "application/json")}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @DeleteMapping("/users/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteUser(@PathVariable @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        log.debug("REST request to delete User: {}", login);

        userService.deleteUser(login);
        return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createAlert(applicationName, "userManagement.deleted", login))
                .build();
    }
}
