package com.plutonem.android.fluxc.store;

import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.annotations.action.Action;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UploadStore extends Store {
    @Inject
    public UploadStore(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void onRegister() {
        AppLog.d(T.API, "UploadStore onRegister");
    }

    // Ensure that events reach the UploadStore before their main stores (OrderStore)
    @Subscribe(threadMode = ThreadMode.ASYNC, priority = 1)
    @Override
    public void onAction(Action action) {

    }
}
