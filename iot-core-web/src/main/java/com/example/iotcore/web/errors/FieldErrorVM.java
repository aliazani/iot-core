package com.example.iotcore.web.errors;

import java.io.Serial;
import java.io.Serializable;

public record FieldErrorVM(String objectName, String field, String message) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
