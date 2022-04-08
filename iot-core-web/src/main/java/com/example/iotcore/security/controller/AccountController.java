package com.example.iotcore.security.controller;

import com.example.iotcore.security.SecurityUtils;
import com.example.iotcore.security.controller.vm.KeyAndPasswordVM;
import com.example.iotcore.security.controller.vm.ManagedUserVM;
import com.example.iotcore.security.domain.User;
import com.example.iotcore.security.dto.AdminUserDTO;
import com.example.iotcore.security.dto.PasswordChangeDTO;
import com.example.iotcore.security.mapper.UserMapper;
import com.example.iotcore.security.repository.UserRepository;
import com.example.iotcore.security.service.MailService;
import com.example.iotcore.security.service.UserService;
import com.example.iotcore.web.errors.EmailAlreadyUsedException;
import com.example.iotcore.web.errors.InvalidPasswordException;
import com.example.iotcore.web.errors.LoginAlreadyUsedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Optional;

/**
 * REST controller for managing the current user's account.
 */
@Tag(name = "Account", description = "Account management")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class AccountController {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final UserService userService;

    private final MailService mailService;

    private static boolean isPasswordLengthInvalid(String password) {
        return (
                StringUtils.isEmpty(password) ||
                        password.length() < ManagedUserVM.PASSWORD_MIN_LENGTH ||
                        password.length() > ManagedUserVM.PASSWORD_MAX_LENGTH
        );
    }

    /**
     * {@code POST  /register} : register the user.
     *
     * @param managedUserVM the managed user View Model.
     * @throws InvalidPasswordException  {@code 400 (Bad Request)} if the password is incorrect.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already used.
     */
    @Operation(summary = "Register new account", description = "Register new account",
            responses = {
                    @ApiResponse(
                            description = "New user created successfully"
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Bad request (invalid password, invalid email, invalid login," +
                                    " email is already used, login is already used)")
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(examples = {@ExampleObject(name = "ManagedUserVM",
                    value = "{\"login\": \"string\", \"email\": \"string@email.com\", \"password\": \"string\"," +
                            "\"langKey\": \"string\"}")}))
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        if (isPasswordLengthInvalid(managedUserVM.getPassword()))
            throw new InvalidPasswordException();

        User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
        mailService.sendActivationEmail(user);
    }

    /**
     * {@code GET  /activate} : activate the registered user.
     *
     * @param key the activation key.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be activated.
     */

    @Operation(summary = "Activate new account", description = "Activate new account",
            responses = {
                    @ApiResponse(
                            description = "New user account activated successfully"
                    ),
                    @ApiResponse(responseCode = "500",
                            description = "Internal Server Error(user couldn't be activated)",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/activate")
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);

        if (user.isEmpty())
            throw new AccountControllerException("No user was found for this activation key");
    }

    /**
     * {@code GET  /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @Operation(summary = "Check If the user is authenticated", description = "Check If the user is authenticated",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(description = "returned username authenticated successfully")
            }
    )
    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");

        return request.getRemoteUser();
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @return the current user.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user couldn't be returned.
     */

    @Operation(summary = "Get the current user", description = "Get the current user",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(description = "current user returned successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AdminUserDTO.class))
                    ),
                    @ApiResponse(responseCode = "500",
                            description = "Internal Server Error(user couldn't be returned)",
                            content = @Content(schema = @Schema(hidden = true))
                    )
            }
    )
    @GetMapping("/account")
    public AdminUserDTO getAccount() {
        return userService
                .getUserWithAuthorities()
                .map(userMapper::toAdminUserDTO)
                .orElseThrow(() -> new AccountControllerException("User could not be found"));
    }

    /**
     * {@code POST  /account} : update the current user information.
     *
     * @param adminUserDTO the current user information.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws RuntimeException          {@code 500 (Internal Server Error)} if the user login wasn't found.
     */

    @Operation(summary = "Update the current user information", description = "Update the current user information",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(description = "current user information updated successfully"),
                    @ApiResponse(responseCode = "400",
                            description = "Email is already used"),
                    @ApiResponse(responseCode = "500",
                            description = "Internal Server Error(user login wasn't found)")
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(examples = {@ExampleObject(name = "AdminUserDTO",
                    value = "{\"login\": \"string\", \"firstName\": \"string\",\"lastName\": \"string\"" +
                            ",\"email\": \"string@email.com\",\"imageUrl\": \"string\",\"langKey\": \"string\"}")}))
    @PostMapping("/account")
    public void saveAccount(@Valid @RequestBody AdminUserDTO adminUserDTO) {
        String userLogin = SecurityUtils
                .getCurrentUserLogin()
                .orElseThrow(() -> new AccountControllerException("Current user login not found"));

        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(adminUserDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin)))
            throw new EmailAlreadyUsedException();
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (user.isEmpty())
            throw new AccountControllerException("User could not be found");

        userService.updateUser(
                adminUserDTO.getFirstName(),
                adminUserDTO.getLastName(),
                adminUserDTO.getEmail(),
                adminUserDTO.getLangKey(),
                adminUserDTO.getImageUrl()
        );
    }

    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */

    @Operation(summary = "Change password", description = "Change the current user's password",
            security = {@SecurityRequirement(name = "bearer-key")},
            responses = {
                    @ApiResponse(description = "current user's password changed successfully"),
                    @ApiResponse(responseCode = "400",
                            description = "New password's length is not valid")
            }
    )
    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (isPasswordLengthInvalid(passwordChangeDto.getNewPassword()))
            throw new InvalidPasswordException();

        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code POST   /account/reset-password/init} : Email reset the password of the user.
     *
     * @param mail the mail of the user.
     */

    @Operation(summary = "Email reset the password", description = "Email reset the password of the user",
            responses = {
                    @ApiResponse(description = "Email reset password sent successfully")
            }
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(schema = @Schema(implementation = String.class),
                    examples = @ExampleObject(value = "string@email.com - REMOVE QUOTES BEFORE SENDING REQUEST!")))
    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody String mail) {
        Optional<User> user = userService.requestPasswordReset(mail);

        if (user.isPresent())
            mailService.sendPasswordResetMail(user.get());
        else
            log.warn("Password reset requested for non existing mail");
        // Pretend the request has been successful to prevent checking which emails really exist
        // but log that an invalid attempt has been made
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish resetting the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException         {@code 500 (Internal Server Error)} if the password could not be reset.
     */
    @Operation(summary = "Reset password", description = "Reset password with key",
            responses = {
                    @ApiResponse(description = "Password updated successfully"),
                    @ApiResponse(responseCode = "400",
                            description = "New password length is not valid"),
                    @ApiResponse(responseCode = "500",
                            description = "Internal Server Error(No user found to reset password)")
            }
    )
    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (isPasswordLengthInvalid(keyAndPassword.getNewPassword()))
            throw new InvalidPasswordException();

        Optional<User> user = userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (user.isEmpty())
            throw new AccountControllerException("No user was found for this reset key");
    }

    private static class AccountControllerException extends RuntimeException {

        private AccountControllerException(String message) {
            super(message);
        }
    }
}
