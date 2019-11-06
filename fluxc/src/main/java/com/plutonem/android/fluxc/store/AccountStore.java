package com.plutonem.android.fluxc.store;

import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.action.AccountAction;
import com.plutonem.android.fluxc.action.AuthenticationAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.network.rest.auth.AccessToken;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In-memory based and persisted in SQLite.
 */
@Singleton
public class AccountStore extends Store {

    // Fields
    private AccessToken mAccessToken;

    @Inject
    public AccountStore(Dispatcher dispatcher, AccessToken accessToken) {
        super(dispatcher);
        mAccessToken = accessToken;
    }

    @Override
    public void onRegister() {
//        AppLog.d(T.API, "AccountStore onRegister");
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    @Override
    public void onAction(Action action) {
        IAction actionType = action.getType();
        if (actionType instanceof AccountAction) {
//            onAccountAction((AccountAction) actionType, action.getPayload());
        }
        if (actionType instanceof AuthenticationAction) {
//            onAuthenticationAction((AuthenticationAction) actionType, action.getPayload());
        }
    }

    /**
     * Can be used to check if Account is signed into Plutonem
     */
    public boolean hasAccessToken() {
        return mAccessToken.exists();
    }
}
