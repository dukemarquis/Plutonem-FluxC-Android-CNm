package com.plutonem.android.fluxc.network.rest.plutonem;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.network.BaseRequest;
import com.plutonem.android.fluxc.network.BaseRequest.OnParseErrorListener;
import com.plutonem.android.fluxc.network.UserAgent;
import com.plutonem.android.fluxc.network.rest.plutonem.auth.AccessToken;
import com.plutonem.android.fluxc.utils.ErrorUtils.OnUnexpectedError;

import org.wordpress.android.util.LanguageUtils;

public abstract class BasePlutonemRestClient {
    private static final String PLUTONEM_V2_PREFIX = "/plutonem/v2";
    private static final String LOCALE_PARAM_NAME_FOR_V1 = "locale";
    private static final String LOCALE_PARAM_NAME_FOR_V2 = "_locale";

    private AccessToken mAccessToken;
    private final RequestQueue mRequestQueue;

    protected final Context mAppContext;
    protected final Dispatcher mDispatcher;
    protected UserAgent mUserAgent;

    private OnParseErrorListener mOnParseErrorListener;

//    private OnParseErrorListener mOnParseErrorListener;

    public BasePlutonemRestClient(Context appContext, Dispatcher dispatcher, RequestQueue requestQueue,
                                  AccessToken accessToken, UserAgent userAgent) {
        mRequestQueue = requestQueue;
        mDispatcher = dispatcher;
        mAccessToken = accessToken;
        mUserAgent = userAgent;
        mAppContext = appContext;
//        mOnAuthFailedListener = new OnAuthFailedListener() {
//            @Override
//            public void onAuthFailed(AuthenticateErrorPayload authError) {
//                mDispatcher.dispatch(AuthenticationActionBuilder.newAuthenticateErrorAction(authError));
//            }
//        };
        mOnParseErrorListener = new OnParseErrorListener() {
            @Override
            public void onParseError(OnUnexpectedError event) {
                mDispatcher.emitChange(event);
            }
        };
//        mOnJetpackTunnelTimeoutListener = new OnJetpackTunnelTimeoutListener() {
//            @Override
//            public void onJetpackTunnelTimeout(OnJetpackTimeoutError onTimeoutError) {
//                mDispatcher.emitChange(onTimeoutError);
//            }
//        };
    }

    public Request add(PlutonemGsonRequest request) {
        // Add "locale=xx_XX" query parameter to all request by default
        return add(request, true);
    }

    protected Request add(PlutonemGsonRequest request, boolean addLocaleParameter) {
        if (addLocaleParameter) {
            addLocaleToRequest(request);
        }
        // TODO: If !mAccountToken.exists() then trigger the mOnAuthFailedListener
        return addRequest(setRequestAuthParams(request, true));
    }

    private PlutonemGsonRequest setRequestAuthParams(PlutonemGsonRequest request, boolean shouldAuth) {
//        request.setOnAuthFailedListener(mOnAuthFailedListener);
        request.setOnParseErrorListener(mOnParseErrorListener);
//        request.setOnJetpackTunnelTimeoutListener(mOnJetpackTunnelTimeoutListener);
        request.setUserAgent(mUserAgent.getUserAgent());
        request.setAccessToken(shouldAuth ? mAccessToken.get() : null);
        return request;
    }

    private Request addRequest(BaseRequest request) {
        if (request.shouldCache() && request.shouldForceUpdate()) {
            mRequestQueue.getCache().invalidate(request.mUri.toString(), true);
        }
        return mRequestQueue.add(request);
    }

    private void addLocaleToRequest(BaseRequest request) {
        String url = request.getUrl();
        // Sanity check
        if (url != null) {
            // PLUTONEM V2 endpoints use a different locale parameter than other endpoints
            String localeParamName =
                    url.contains(PLUTONEM_V2_PREFIX) ? LOCALE_PARAM_NAME_FOR_V2 : LOCALE_PARAM_NAME_FOR_V1;
            request.addQueryParameter(localeParamName, LanguageUtils.getPatchedCurrentDeviceLanguage(mAppContext));
        }
    }
}
