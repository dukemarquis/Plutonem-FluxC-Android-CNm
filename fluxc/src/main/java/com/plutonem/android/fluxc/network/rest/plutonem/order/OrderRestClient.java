package com.plutonem.android.fluxc.network.rest.plutonem.order;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.network.UserAgent;
import com.plutonem.android.fluxc.network.rest.plutonem.BasePlutonemRestClient;
import com.plutonem.android.fluxc.network.rest.plutonem.auth.AccessToken;

import javax.inject.Singleton;

@Singleton
public class OrderRestClient extends BasePlutonemRestClient {
    public OrderRestClient(Context appContext, Dispatcher dispatcher, RequestQueue requestQueue, AccessToken accessToken,
                           UserAgent userAgent) {
        super(appContext, dispatcher, requestQueue, accessToken, userAgent);
    }
}
