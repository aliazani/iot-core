package com.example.iotcore.security.controller;

import com.example.iotcore.security.dto.UserDTO;
import com.example.iotcore.security.service.UserService;
import com.example.iotcore.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Tag(name = "Public User", description = "Public user management")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class PublicUserController {

    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList("id", "login", "firstName", "lastName", "email", "activated", "langKey")
    );

    private final UserService userService;

    /**
     * {@code GET /users} : get all users with only the public information - calling this are allowed for anyone.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */

    @Operation(summary = "Get all public users paged", description = "Get all public users paged",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "a page of users retrieved successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDTO.class))}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllPublicUsers(@org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get all public User names");

        if (!onlyContainsAllowedProperties(pageable))
            return ResponseEntity.badRequest().build();

        final Page<UserDTO> page = userService.getAllPublicUsers(pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

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
     * Gets a list of all roles.
     *
     * @return a string list of all roles.
     */

    @Operation(summary = "Get all authorities", description = "Get all authorities",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(responseCode = "200", description = "all authorities retrieved successfully",
                            content = {@Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserDTO.class))}
                    ),
                    @ApiResponse(responseCode = "401", description = "Authentication Failure",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/authorities")
    public List<String> getAuthorities() {
        return userService.getAuthorities();
    }
}
