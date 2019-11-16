package com.plutonem.android.fluxc.network;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.plutonem.android.fluxc.FluxCError;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseRequest<T> extends Request<T> {
    public static final int DEFAULT_REQUEST_TIMEOUT = 30000;

    public Uri mUri;

    public interface BaseErrorListener {
        void onErrorResponse(@NonNull BaseNetworkError error);
    }

//    public interface OnParseErrorListener {
//        void onParseError(OnUnexpectedError event);
//    }

    private static final String USER_AGENT_HEADER = "User-Agent";

    protected final Map<String, String> mHeaders = new HashMap<>(2);
    private BaseErrorListener mErrorListener;

    private boolean mResetCache;
    private int mCacheTtl;
    private int mCacheSoftTtl;

    public static class BaseNetworkError implements FluxCError {
        public GenericErrorType type;
        public String message;
        public VolleyError volleyError;

        public BaseNetworkError(@NonNull BaseNetworkError error) {
            this.message = error.message;
            this.type = error.type;
            this.volleyError = error.volleyError;
        }

        public BaseNetworkError(@NonNull GenericErrorType error, @NonNull String message,
                                @NonNull VolleyError volleyError) {
            this.message = message;
            this.type = error;
            this.volleyError = volleyError;
        }

        public BaseNetworkError(@NonNull GenericErrorType error, @NonNull VolleyError volleyError) {
            this.message = "";
            this.type = error;
            this.volleyError = volleyError;
        }

        public BaseNetworkError(@NonNull VolleyError volleyError) {
            this.type = GenericErrorType.UNKNOWN;
            this.message = "";
            this.volleyError = volleyError;
        }

        public BaseNetworkError(@NonNull GenericErrorType error) {
            this.type = error;
        }

        public boolean isGeneric() {
            return type != null;
        }

        public boolean hasVolleyError() {
            return volleyError != null;
        }
    }

    public enum GenericErrorType {
        // Network Layer
        TIMEOUT,
        NO_CONNECTION,
        NETWORK_ERROR,

        // HTTP Layer
        NOT_FOUND,
        CENSORED,
        SERVER_ERROR,
        INVALID_SSL_CERTIFICATE,
        HTTP_AUTH_ERROR,

        // Web Application Layer
        INVALID_RESPONSE,
        AUTHORIZATION_REQUIRED,
        NOT_AUTHENTICATED,
        PARSE_ERROR,

        // Other
        UNKNOWN,
    }

    public BaseRequest(int method, @NonNull String url, BaseErrorListener errorListener) {
        super(method, url, null);
        if (url != null) {
            mUri = Uri.parse(url);
        } else {
            mUri = Uri.EMPTY;
        }
        mErrorListener = errorListener;
        // Make sure all our custom Requests are never cached.
        setShouldCache(false);
        setRetryPolicy(new DefaultRetryPolicy(DEFAULT_REQUEST_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    public void addQueryParameter(String key, String value) {
        mUri = mUri.buildUpon().appendQueryParameter(key, value).build();
    }

    public void addQueryParameters(Map<String, String> parameters) {
        if (parameters == null) return;
        Uri.Builder builder = mUri.buildUpon();
        for (String key : parameters.keySet()) {
            builder.appendQueryParameter(key, parameters.get(key));
        }
        mUri = builder.build();
    }

    /**
     * Enable caching for this request. The {@code timeToLive} and {@code softTimeToLive} params correspond to the
     * {@code ttl} and {@code softTtl} fields in {@link Cache}.
     *
     * @param timeToLive     the amount of time before the cache expires
     * @param softTimeToLive the amount of time before the cache soft expires (the cached result is returned,
     *                       but a network request is also dispatched to update the cache)
     */
    public void enableCaching(int timeToLive, int softTimeToLive) {
        setShouldCache(true);
        mCacheTtl = timeToLive;
        mCacheSoftTtl = softTimeToLive;
    }

    /**
     * Returns true if this request should ignore the cache and force a fresh update over the network.
     */
    public boolean shouldForceUpdate() {
        return mResetCache;
    }

    public void setUserAgent(String userAgent) {
        mHeaders.put(USER_AGENT_HEADER, userAgent);
    }

    /**
     * Generate a cache entry for this request.
     * <p>
     * If caching has been enabled through {@link BaseRequest#enableCaching(int, int)}, the expiry parameters that were
     * given are used to configure the cache entry.
     * <p>
     * Otherwise, just generate a cache entry from the response's cache headers (default behaviour).
     */
    protected Cache.Entry createCacheEntry(NetworkResponse response) {
        Cache.Entry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);

        if (!shouldCache()) {
            return cacheEntry;
        }

        if (cacheEntry == null) {
            cacheEntry = new Cache.Entry();

            String headerValue = response.headers.get("Date");
            if (headerValue != null) {
                cacheEntry.serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
            }

            headerValue = response.headers.get("Last-Modified");
            if (headerValue != null) {
                cacheEntry.lastModified = HttpHeaderParser.parseDateAsEpoch(headerValue);
            }

            cacheEntry.data = response.data;
            cacheEntry.responseHeaders = response.headers;
        }

        long now = System.currentTimeMillis();
        cacheEntry.ttl = now + mCacheTtl;
        cacheEntry.softTtl = now + mCacheSoftTtl;

        return cacheEntry;
    }
}
