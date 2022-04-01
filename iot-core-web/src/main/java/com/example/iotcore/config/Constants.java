package com.example.iotcore.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Application constants.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    // Regex for acceptable logins(usernames)
    public static final String LOGIN_REGEX =
            "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM = "SYSTEM";

    public static final String DEFAULT_LANGUAGE = "en";
}
