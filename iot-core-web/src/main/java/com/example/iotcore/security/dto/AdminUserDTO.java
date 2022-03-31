package com.example.iotcore.security.dto;

import com.example.iotcore.config.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Set;

/**
 * A DTO representing a user, with his authorities.
 */
@NoArgsConstructor
@Data
public class AdminUserDTO {

    private Long id;

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @Size(min = 5, max = 254)
    private String email;

    @Size(max = 256)
    private String imageUrl;

    private boolean activated = false;

    @Size(min = 2, max = 10)
    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Set<String> authorities;
//    public AdminUserDTO(User user) {
//        this.id = user.getId();
//        this.login = user.getLogin();
//        this.firstName = user.getFirstName();
//        this.lastName = user.getLastName();
//        this.email = user.getEmail();
//        this.activated = user.isActivated();
//        this.imageUrl = user.getImageUrl();
//        this.langKey = user.getLangKey();
//        this.createdBy = user.getCreatedBy();
//        this.createdDate = user.getCreatedDate();
//        this.lastModifiedBy = user.getLastModifiedBy();
//        this.lastModifiedDate = user.getLastModifiedDate();
//        this.authorities = user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet());
//    }
}
