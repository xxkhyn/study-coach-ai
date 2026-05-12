package com.studycoachai.exception;

public class AppConfigurationException extends RuntimeException {
    public AppConfigurationException(String message) {
        super(message);
    }

    public AppConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
