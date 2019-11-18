package com.plutonem.android.fluxc.module;

import android.content.Context;

import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.network.OkHttpStack;
import com.plutonem.android.fluxc.network.UserAgent;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient;
import com.plutonem.android.fluxc.network.rest.plutonem.auth.AccessToken;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class ReleaseNetworkModule {
    private static final String DEFAULT_CACHE_DIR = "volley-fluxc";
    private static final int NETWORK_THREAD_POOL_SIZE = 10;

    private RequestQueue newRequestQueue(OkHttpClient.Builder okHttpClientBuilder, Context appContext) {
        File cacheDir = new File(appContext.getCacheDir(), DEFAULT_CACHE_DIR);
        Network network = new BasicNetwork(new OkHttpStack(okHttpClientBuilder));
        RequestQueue queue = new RequestQueue(new DiskBasedCache(cacheDir), network, NETWORK_THREAD_POOL_SIZE);
        queue.start();
        return queue;
    }

    @Singleton
    @Named("regular")
    @Provides
    public RequestQueue provideRequestQueue(@Named("regular") OkHttpClient.Builder okHttpClientBuilder,
                                            Context appContext) {
        return newRequestQueue(okHttpClientBuilder, appContext);
    }

    @Singleton
    @Provides
    public AccountRestClient provideAccountRestClient(Context appContext, Dispatcher dispatcher,
                                                      @Named("regular") RequestQueue requestQueue,
                                                      AccessToken token, UserAgent userAgent) {
        return new AccountRestClient(appContext, dispatcher, requestQueue, token, userAgent);
    }
}
