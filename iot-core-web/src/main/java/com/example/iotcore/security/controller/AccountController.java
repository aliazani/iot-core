package com.example.iotcore.security.controller;

import com.example.iotcore.security.SecurityUtils;
import com.example.iotcore.security.controller.vm.KeyAndPasswordVM;
import com.example.iotcore.security.controller.vm.ManagedUserVM;
import com.example.iotcore.security.domain.User;
import com.example.iotcore.security.dto.AdminUserDTO;
import com.example.iotcore.security.dto.PasswordChangeDTO;
import com.example.iotcore.security.exception.EmailAlreadyUsedException;
import com.example.iotcore.security.exception.InvalidPasswordException;
import com.example.iotcore.security.exception.LoginAlreadyUsedException;
import com.example.iotcore.security.mapper.UserMapper;
import com.example.iotcore.security.repository.UserRepository;
import com.example.iotcore.security.service.MailService;
import com.example.iotcore.security.service.UserService;
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
    @GetMapping("/account")
    public AdminUserDTO getAccount() {
        return userService
                .getUserWithAuthorities()
                .map(userMapper::toAdminUserDTO)
//                .map(AdminUserDTO::new)
                .orElseThrow(() -> new AccountControllerException("User could not be found"));
    }

    /**
     * {@code POST  /account} : update the current user information.
     *
     * @param adminUserDTO the current user information.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already used.
     * @throws RuntimeException          {@code 500 (Internal Server Error)} if the user login wasn't found.
     */
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
    @PostMapping(path = "/account/change-password")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (isPasswordLengthInvalid(passwordChangeDto.getNewPassword()))
            throw new InvalidPasswordException();

        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     */
    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody String mail) {
        Optional<User> user = userService.requestPasswordReset(mail);

        if (user.isPresent())
            mailService.sendPasswordResetMail(user.get());
        else {
            // Pretend the request has been successful to prevent checking which emails really exist
            // but log that an invalid attempt has been made
            log.warn("Password reset requested for non existing mail");
        }
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws RuntimeException         {@code 500 (Internal Server Error)} if the password could not be reset.
     */
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
