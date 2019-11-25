package com.plutonem.android.fluxc.network.rest.plutonem.auth;

import com.plutonem.android.fluxc.store.AccountStore.AuthenticationErrorType;

public class Authenticator {
    public static AuthenticationErrorType pnApiErrorToAuthenticationError(String errorType, String errorMessage) {
        AuthenticationErrorType error = AuthenticationErrorType.fromString(errorType);
        // Special cases for vague error types
        if (error == AuthenticationErrorType.INVALID_REQUEST) {
            // Try to parse the error message to specify the error
            if (errorMessage.contains("Incorrect username or password.")) {
                return AuthenticationErrorType.INCORRECT_USERNAME_OR_PASSWORD;
            }
        }
        return error;
    }
}
