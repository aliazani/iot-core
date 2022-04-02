package com.example.iotcore.security.service;

import com.example.iotcore.config.Constants;
import com.example.iotcore.security.AuthoritiesConstants;
import com.example.iotcore.security.SecurityUtils;
import com.example.iotcore.security.domain.Authority;
import com.example.iotcore.security.domain.User;
import com.example.iotcore.security.dto.AdminUserDTO;
import com.example.iotcore.security.dto.UserDTO;
import com.example.iotcore.security.exception.UsernameAlreadyUsedException;
import com.example.iotcore.security.mapper.AuthorityMapper;
import com.example.iotcore.security.mapper.UserMapper;
import com.example.iotcore.security.repository.AuthorityRepository;
import com.example.iotcore.security.repository.UserRepository;
import com.example.iotcore.web.errors.EmailAlreadyUsedException;
import com.example.iotcore.web.errors.InvalidPasswordException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final AuthorityMapper authorityMapper;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    private final CacheManager cacheManager;

    private static String generateRandomAlphaNumericString() {
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(new byte[64]);

        return RandomStringUtils.random(20, 0, 0, true, true, null, secureRandom);
    }

    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);

        return userRepository
                .findOneByActivationKey(key)
                .map(user -> {
                    // activate given user for the registration key.
                    user.setActivated(true);
                    user.setActivationKey(null);
                    this.clearUserCaches(user);

                    log.debug("Activated user: {}", user);

                    return user;
                });
    }

    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);

        return userRepository
                .findOneByResetKey(key)
                .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setResetKey(null);
                    user.setResetDate(null);
                    this.clearUserCaches(user);

                    return user;
                });
    }

    public Optional<User> requestPasswordReset(String mail) {
        return userRepository
                .findOneByEmailIgnoreCase(mail)
                .filter(User::isActivated)
                .map(user -> {
                    user.setResetKey(generateRandomAlphaNumericString());
                    user.setResetDate(Instant.now());
                    this.clearUserCaches(user);
                    return user;
                });
    }

    public User registerUser(AdminUserDTO adminUserDTO, String password) {
        userRepository
                .findOneByLogin(adminUserDTO.getLogin().toLowerCase())
                .ifPresent(existingUser -> {
                    boolean removed = removeNonActivatedUser(existingUser);
                    if (!removed) {
                        throw new UsernameAlreadyUsedException();
                    }
                });

        userRepository
                .findOneByEmailIgnoreCase(adminUserDTO.getEmail())
                .ifPresent(existingUser -> {
                    boolean removed = removeNonActivatedUser(existingUser);
                    if (!removed) {
                        throw new EmailAlreadyUsedException();
                    }
                });

        User newUser = userMapper.fromAdminUserDTOToUserEntity(adminUserDTO);
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setPassword(encryptedPassword);
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(generateRandomAlphaNumericString());
        Set<Authority> authorities = new HashSet<>();
        authorityRepository.findById(AuthoritiesConstants.USER).ifPresent(authorities::add);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        this.clearUserCaches(newUser);

        log.debug("Created Information for User: {}", newUser);

        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) return false;

        userRepository.delete(existingUser);
        userRepository.flush();
        this.clearUserCaches(existingUser);

        return true;
    }

    public User createUser(AdminUserDTO adminUserDTO) {
        User user = userMapper.fromAdminUserDTOToUserEntity(adminUserDTO);
        if (adminUserDTO.getLangKey() == null) user.setLangKey(Constants.DEFAULT_LANGUAGE);
        else user.setLangKey(adminUserDTO.getLangKey());
        String encryptedPassword = passwordEncoder.encode(generateRandomAlphaNumericString());
        user.setPassword(encryptedPassword);
        user.setResetKey(generateRandomAlphaNumericString());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        if (adminUserDTO.getAuthorities() != null) {
            Set<Authority> authorities = adminUserDTO
                    .getAuthorities()
                    .stream()
                    .map(authorityRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            user.setAuthorities(authorities);
        }
        userRepository.save(user);
        this.clearUserCaches(user);

        log.debug("Created Information for User: {}", user);

        return user;
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param adminUserDTO user to update.
     * @return updated user.
     */
    public Optional<AdminUserDTO> updateUser(AdminUserDTO adminUserDTO) {
        return Optional
                .of(userRepository.findById(adminUserDTO.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(user -> {
                    this.clearUserCaches(user);
                    user = userMapper.fromAdminUserDTOToUserEntity(adminUserDTO);
                    Set<Authority> managedAuthorities = user.getAuthorities();
                    managedAuthorities.clear();
                    adminUserDTO
                            .getAuthorities()
                            .stream()
                            .map(authorityRepository::findById)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(managedAuthorities::add);

//                    user.setAuthorities(managedAuthorities);
                    this.clearUserCaches(user);

                    log.debug("Changed Information for User: {}", user);

                    return user;
                })
                .map(userMapper::toAdminUserDTO);
    }

    public void deleteUser(String login) {
        userRepository
                .findOneByLogin(login)
                .ifPresent(user -> {
                    userRepository.delete(user);
                    this.clearUserCaches(user);

                    log.debug("Deleted User: {}", user);
                });
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils
                .getCurrentUserLogin()
                .flatMap(userRepository::findOneByLogin)
                .ifPresent(user -> {
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    if (email != null) {
                        user.setEmail(email.toLowerCase());
                    }
                    user.setLangKey(langKey);
                    user.setImageUrl(imageUrl);
                    this.clearUserCaches(user);

                    log.debug("Changed Information for User: {}", user);
                });
    }

    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils
                .getCurrentUserLogin()
                .flatMap(userRepository::findOneByLogin)
                .ifPresent(user -> {
                    String currentEncryptedPassword = user.getPassword();
                    if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword))
                        throw new InvalidPasswordException();
                    String encryptedPassword = passwordEncoder.encode(newPassword);
                    user.setPassword(encryptedPassword);
                    this.clearUserCaches(user);

                    log.debug("Changed password for User: {}", user);
                });
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getAllManagedUsers(Pageable pageable) {
        return userRepository
                .findAll(pageable)
                .map(userMapper::toAdminUserDTO);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllPublicUsers(Pageable pageable) {
        return userRepository
                .findAllByIdNotNullAndActivatedIsTrue(pageable)
                .map(userMapper::toUserDTO);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities() {
        return SecurityUtils
                .getCurrentUserLogin()
                .flatMap(userRepository::findOneWithAuthoritiesByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        userRepository
                .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
                .forEach(user -> {
                    log.debug("Deleting not activated user {}", user.getLogin());

                    userRepository.delete(user);
                    this.clearUserCaches(user);
                });
    }

    /**
     * Gets a list of all the authorities.
     *
     * @return a list of all the authorities.
     */
    @Transactional(readOnly = true)
    public List<String> getAuthorities() {
        return authorityRepository.findAll()
                .stream()
                .map(authorityMapper::authorityToString)
                .toList();
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evict(user.getLogin());

        if (user.getEmail() != null)
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evict(user.getEmail());
    }
}
