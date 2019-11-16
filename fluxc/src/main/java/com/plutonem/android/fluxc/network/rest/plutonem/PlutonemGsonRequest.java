package com.plutonem.android.fluxc.network.rest.plutonem;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.Response;
import com.plutonem.android.fluxc.network.rest.GsonRequest;

import java.lang.reflect.Type;
import java.util.Map;

public class PlutonemGsonRequest<T> extends GsonRequest<T> {
    public interface PlutonemErrorListener {
        void onErrorResponse(@NonNull PlutonemGsonNetworkError error);
    }

    public static final String REST_AUTHORIZATION_HEADER = "Authorization";
    public static final String REST_AUTHORIZATION_FORMAT = "Bearer %s";

    public static class PlutonemGsonNetworkError extends BaseNetworkError {
        public String apiError;
        public PlutonemGsonNetworkError(BaseNetworkError error) {
            super(error);
            this.apiError = "";
        }
    }

    private PlutonemGsonRequest(int method, String url, Map<String, String> params, Map<String, Object> body,
                             Class<T> clazz, Type type, Response.Listener<T> listener, BaseErrorListener errorListener) {
        super(method, params, body, url, clazz, type, listener, errorListener);
        // If it's a GET request, add the parameters to the URL
        if (method == Request.Method.GET) {
            addQueryParameters(params);
        }
    }

    /**
     * Creates a new GET request.
     * @param url the request URL
     * @param params the parameters to append to the request URL
     * @param clazz the class defining the expected response
     * @param listener the success listener
     * @param errorListener the error listener
     */
    public static <T> PlutonemGsonRequest<T> buildGetRequest(String url, Map<String, String> params, Class<T> clazz,
                                                          Response.Listener<T> listener, PlutonemErrorListener errorListener) {
        return new PlutonemGsonRequest<>(Request.Method.GET, url, params, null, clazz, null, listener,
                wrapInBaseListener(errorListener));
    }

    public static <T> PlutonemGsonRequest<T> buildGetRequest(String url, Map<String, String> params, Type type,
                                                          Response.Listener<T> listener, PlutonemErrorListener errorListener) {
        return new PlutonemGsonRequest<>(Request.Method.GET, url, params, null, null, type, listener,
                wrapInBaseListener(errorListener));
    }

    private static BaseErrorListener wrapInBaseListener(final PlutonemErrorListener plutonemErrorListener) {
        return new BaseErrorListener() {
            @Override
            public void onErrorResponse(@NonNull BaseNetworkError error) {
                if (plutonemErrorListener != null) {
                    plutonemErrorListener.onErrorResponse((PlutonemGsonNetworkError) error);
                }
            }
        };
    }

    public void setAccessToken(String token) {
        if (token == null) {
            mHeaders.remove(REST_AUTHORIZATION_HEADER);
        } else {
            mHeaders.put(REST_AUTHORIZATION_HEADER, String.format(REST_AUTHORIZATION_FORMAT, token));
        }
    }
}
