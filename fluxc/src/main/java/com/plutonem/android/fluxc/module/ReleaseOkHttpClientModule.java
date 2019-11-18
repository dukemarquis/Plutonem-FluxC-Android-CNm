package com.plutonem.android.fluxc.module;

import javax.inject.Named;

import dagger.Provides;
import okhttp3.OkHttpClient;

public class ReleaseOkHttpClientModule {
    @Provides
    @Named("regular")
    public OkHttpClient.Builder provideOkHttpClientBuilder() {
        return new OkHttpClient.Builder();
    }
}
