package com.plutonem.android.fluxc.module;

import com.plutonem.android.fluxc.Dispatcher;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ReleaseBaseModule {
    @Singleton
    @Provides
    public Dispatcher provideDispatcher() {
        return new Dispatcher();
    }
}

