package com.example.iotcore.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderUtil {
    /**
     * <p>createAlert.</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     * @param message         a {@link java.lang.String} object.
     * @param param           a {@link java.lang.String} object.
     * @return a {@link org.springframework.http.HttpHeaders} object.
     */
    public static HttpHeaders createAlert(String applicationName, String message, String param) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-%s-alert".formatted(applicationName), message);

        try {
            headers.add("X-%s-params".formatted(applicationName),
                    URLEncoder.encode(param, StandardCharsets.UTF_8.toString())
            );
        } catch (UnsupportedEncodingException e) {
            // StandardCharsets are supported by every Java implementation so this exception will never happen
        }

        return headers;
    }

    /**
     * <p>createEntityCreationAlert.</p>
     *
     * @param applicationName   a {@link java.lang.String} object.
     * @param enableTranslation a boolean.
     * @param entityName        a {@link java.lang.String} object.
     * @param param             a {@link java.lang.String} object.
     * @return a {@link org.springframework.http.HttpHeaders} object.
     */
    public static HttpHeaders createEntityCreationAlert(String applicationName, boolean enableTranslation,
                                                        String entityName, String param) {
        String message = enableTranslation ? "%s.%s.created".formatted(applicationName, entityName)
                : "A new %s is created with identifier %s".formatted(entityName, param);

        return createAlert(applicationName, message, param);
    }

    /**
     * <p>createEntityUpdateAlert.</p>
     *
     * @param applicationName   a {@link java.lang.String} object.
     * @param enableTranslation a boolean.
     * @param entityName        a {@link java.lang.String} object.
     * @param param             a {@link java.lang.String} object.
     * @return a {@link org.springframework.http.HttpHeaders} object.
     */
    public static HttpHeaders createEntityUpdateAlert(String applicationName, boolean enableTranslation,
                                                      String entityName, String param) {
        String message = enableTranslation ? "%s.%s.updated".formatted(applicationName, entityName)
                : "A %s is updated with identifier %s".formatted(entityName, param);

        return createAlert(applicationName, message, param);
    }

    /**
     * <p>createEntityDeletionAlert.</p>
     *
     * @param applicationName   a {@link java.lang.String} object.
     * @param enableTranslation a boolean.
     * @param entityName        a {@link java.lang.String} object.
     * @param param             a {@link java.lang.String} object.
     * @return a {@link org.springframework.http.HttpHeaders} object.
     */
    public static HttpHeaders createEntityDeletionAlert(String applicationName, boolean enableTranslation,
                                                        String entityName, String param) {
        String message = enableTranslation ? "%s.%s.deleted".formatted(applicationName, entityName)
                : "A %s is deleted with identifier %s".formatted(entityName, param);

        return createAlert(applicationName, message, param);
    }

    /**
     * <p>createFailureAlert.</p>
     *
     * @param applicationName   a {@link java.lang.String} object.
     * @param enableTranslation a boolean.
     * @param entityName        a {@link java.lang.String} object.
     * @param errorKey          a {@link java.lang.String} object.
     * @param defaultMessage    a {@link java.lang.String} object.
     * @return a {@link org.springframework.http.HttpHeaders} object.
     */
    public static HttpHeaders createFailureAlert(String applicationName, boolean enableTranslation,
                                                 String entityName, String errorKey, String defaultMessage) {
        log.error("Entity processing failed, {}", defaultMessage);

        String message = enableTranslation ? "error.%s".formatted(errorKey) : defaultMessage;

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-%s-error".formatted(applicationName), message);
        headers.add("X-%s-params".formatted(applicationName), entityName);

        return headers;
    }
}
