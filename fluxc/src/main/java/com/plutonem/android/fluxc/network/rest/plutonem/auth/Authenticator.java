package com.plutonem.android.fluxc.network.rest.plutonem.auth;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.network.rest.plutonem.reg.Registor;
import com.plutonem.android.fluxc.network.rest.plutonem.reg.Registor.Token;
import com.plutonem.android.fluxc.store.AccountStore.AuthenticationErrorType;

import org.json.JSONException;
import org.json.JSONObject;
import org.wordpress.android.util.AppLog;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Authenticator {
    private static final String PNCOM_OAUTH_PREFIX = "http://39.99.148.207/oauth2";
    private static final String TOKEN_ENDPOINT = PNCOM_OAUTH_PREFIX + "/token";

    public static final String CLIENT_ID_PARAM_NAME = "client_id";
    public static final String CLIENT_SECRET_PARAM_NAME = "client_secret";
    public static final String GRANT_TYPE_PARAM_NAME = "grant_type";
    public static final String USERNAME_PARAM_NAME = "username";
    public static final String PASSWORD_PARAM_NAME = "password";

    public static final String PASSWORD_GRANT_TYPE = "password";

    private final Context mAppContext;
    private final Dispatcher mDispatcher;
    private final RequestQueue mRequestQueue;

    public interface Listener extends Response.Listener<Token> {
    }

    public interface ErrorListener extends Response.ErrorListener {
    }

    public Authenticator(Context appContext, Dispatcher dispatcher, RequestQueue requestQueue) {
        mAppContext = appContext;
        mDispatcher = dispatcher;
        mRequestQueue = requestQueue;
    }

    public void authenticate(String username, String password, String twoStepCode, boolean shouldSendTwoStepSMS,
                             Listener listener, ErrorListener errorListener) {
        TokenRequest tokenRequest = makeRequest(username, password, twoStepCode, shouldSendTwoStepSMS, listener,
                errorListener);
        mRequestQueue.add(tokenRequest);
    }

    public TokenRequest makeRequest(String username, String password, String twoStepCode, boolean shouldSendTwoStepSMS,
                                    Listener listener, ErrorListener errorListener) {
        return new PasswordRequest(null, null, username, password, twoStepCode,
                shouldSendTwoStepSMS, listener, errorListener);
    }

    private static class TokenRequest extends Request<Token> {
        private final Listener mListener;
        protected Map<String, String> mParams = new HashMap<>();

        TokenRequest(String appId, String appSecret, Listener listener, ErrorListener errorListener) {
            super(Method.POST, TOKEN_ENDPOINT, errorListener);
            mListener = listener;
            mParams.put(CLIENT_ID_PARAM_NAME, appId);
            mParams.put(CLIENT_SECRET_PARAM_NAME, appSecret);
        }

        @Override
        public Map<String, String> getParams() {
            return mParams;
        }

        @Override
        public void deliverResponse(Token token) {
            mListener.onResponse(token);
        }

        @Override
        protected Response<Token> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                JSONObject tokenData = new JSONObject(jsonString);
                return Response.success(Token.fromJSONObject(tokenData), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException je) {
                return Response.error(new ParseError(je));
            }
        }
    }

    public static class PasswordRequest extends TokenRequest {
        public PasswordRequest(String appId, String appSecret, String username, String password, String twoStepCode,
                               boolean shouldSendTwoStepSMS, Listener listener, ErrorListener errorListener) {
            super(appId, appSecret, listener, errorListener);
            mParams.put(USERNAME_PARAM_NAME, username);
            mParams.put(PASSWORD_PARAM_NAME, password);
            mParams.put(GRANT_TYPE_PARAM_NAME, PASSWORD_GRANT_TYPE);

            if (!TextUtils.isEmpty(twoStepCode)) {
                mParams.put("pn_otp", twoStepCode);
            } else {
                mParams.put("pn_supports_2fa", "true");
                if (shouldSendTwoStepSMS) {
                    mParams.put("pn_resend_otp", "true");
                }
            }
        }
    }

//    public static class Token {
//        private static final String TOKEN_TYPE_FIELD_NAME = "token_type";
//        private static final String ACCESS_TOKEN_FIELD_NAME = "access_token";
//        private static final String SCOPE_FIELD_NAME = "scope";
//
//        private String mTokenType;
//        private String mScope;
//        private String mAccessToken;
//
//        public Token(String accessToken, String scope, String tokenType) {
//            mAccessToken = accessToken;
//            mScope = scope;
//            mTokenType = tokenType;
//        }
//
//        public String getAccessToken() {
//            return mAccessToken;
//        }
//
//        public String toString() {
//            return getAccessToken();
//        }
//
//        public static Token fromJSONObject(JSONObject tokenJSON) throws JSONException {
//            return new Token(tokenJSON.getString(ACCESS_TOKEN_FIELD_NAME), tokenJSON.getString(SCOPE_FIELD_NAME),
//                    tokenJSON.getString(TOKEN_TYPE_FIELD_NAME));
//        }
//    }

    public static AuthenticationErrorType volleyErrorToAuthenticationError(VolleyError error) {
        if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
            String jsonString = new String(error.networkResponse.data);
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                return jsonErrorToAuthenticationError(jsonObject);
            } catch (JSONException e) {
                AppLog.e(AppLog.T.API, e);
            }
        }
        return AuthenticationErrorType.GENERIC_ERROR;
    }

    public static String volleyErrorToErrorMessage(VolleyError error) {
        if (error != null && error.networkResponse != null && error.networkResponse.data != null) {
            String jsonString = new String(error.networkResponse.data);
            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                return jsonObject.getString("error_description");
            } catch (JSONException e) {
                AppLog.e(AppLog.T.API, e);
            }
        }
        return null;
    }

    public static AuthenticationErrorType jsonErrorToAuthenticationError(JSONObject jsonObject) {
        AuthenticationErrorType error = AuthenticationErrorType.GENERIC_ERROR;
        if (jsonObject != null) {
            String errorType = jsonObject.optString("error", "");
            String errorMessage = jsonObject.optString("error_description", "");
            error = pnApiErrorToAuthenticationError(errorType, errorMessage);
        }
        return error;
    }

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
