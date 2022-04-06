package com.example.iotcore.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A DTO representing a password change required data - current and new password.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PasswordChangeDTO {

    private String currentPassword;

    private String newPassword;
}
