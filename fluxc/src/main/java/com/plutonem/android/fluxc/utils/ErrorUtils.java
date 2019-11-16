package com.plutonem.android.fluxc.utils;

import org.wordpress.android.util.AppLog;

import java.util.HashMap;
import java.util.Map;

public class ErrorUtils {
    public static class OnUnexpectedError {
        public static final String KEY_URL = "url";
        public static final String KEY_RESPONSE = "response";

        public Exception exception;
        public String description;
        public Map<String, String> extras = new HashMap<>();
        public AppLog.T type;

        public OnUnexpectedError(Exception exception) {
            this(exception, "");
        }

        public OnUnexpectedError(Exception exception, String description) {
            this(exception, description, AppLog.T.API);
        }

        public OnUnexpectedError(Exception exception, String description, AppLog.T type) {
            this.exception = exception;
            this.description = description;
            this.type = type;
        }

        public void addExtra(String key, String value) {
            extras.put(key, value);
        }
    }
}
