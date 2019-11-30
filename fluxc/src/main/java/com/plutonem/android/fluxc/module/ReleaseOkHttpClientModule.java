package com.plutonem.android.fluxc.module;

import com.plutonem.android.fluxc.network.MemorizingTrustManager;

import org.wordpress.android.util.AppLog;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.inject.Named;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module
public class ReleaseOkHttpClientModule {
    @Provides
    @Named("regular")
    public OkHttpClient.Builder provideOkHttpClientBuilder() {
        return new OkHttpClient.Builder();
    }

    @Provides
    @Named("custom-ssl")
    public OkHttpClient.Builder provideOkHttpClientBuilderCustomSSL(MemorizingTrustManager memorizingTrustManager) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{memorizingTrustManager}, new SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            builder.sslSocketFactory(sslSocketFactory);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            AppLog.e(AppLog.T.API, e);
        }
        return builder;
    }
}
