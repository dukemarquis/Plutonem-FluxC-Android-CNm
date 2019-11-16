package com.plutonem.android.fluxc.store;

import androidx.annotation.NonNull;

import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.action.AccountAction;
import com.plutonem.android.fluxc.action.AuthenticationAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient.IsAvailable;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient.IsAvailableResponsePayload;
import com.plutonem.android.fluxc.network.rest.plutonem.auth.AccessToken;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In-memory based and persisted in SQLite.
 */
@Singleton
public class AccountStore extends Store {
    // OnChanged Events
    public static class OnAvailabilityChecked extends OnChanged<IsAvailableError> {
        public IsAvailable type;
        public String value;
        public boolean isAvailable;

        public OnAvailabilityChecked(IsAvailable type, String value, boolean isAvailable) {
            this.type = type;
            this.value = value;
            this.isAvailable = isAvailable;
        }
    }

    public static class IsAvailableError implements OnChangedError {
        public IsAvailableErrorType type;
        public String message;

        public IsAvailableError(@NonNull String type, @NonNull String message) {
            this.type = IsAvailableErrorType.fromString(type);
            this.message = message;
        }
    }

    public enum IsAvailableErrorType {
        INVALID,
        GENERIC_ERROR;

        public static IsAvailableErrorType fromString(String string) {
            if (string != null) {
                for (IsAvailableErrorType v : IsAvailableErrorType.values()) {
                    if (string.equalsIgnoreCase(v.name())) {
                        return v;
                    }
                }
            }
            return GENERIC_ERROR;
        }
    }


    // Fields
    private AccountRestClient mAccountRestClient;
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
            onAccountAction((AccountAction) actionType, action.getPayload());
        }
        if (actionType instanceof AuthenticationAction) {
//            onAuthenticationAction((AuthenticationAction) actionType, action.getPayload());
        }
    }

    private void onAccountAction(AccountAction actionType, Object payload) {
        switch (actionType) {
            case IS_AVAILABLE_PHONE:
                mAccountRestClient.isAvailable((String) payload, IsAvailable.PHONE);
                break;
            case CHECKED_IS_AVAILABLE:
                handleCheckedIsAvailable((IsAvailableResponsePayload) payload);
                break;
        }
    }

    private void handleCheckedIsAvailable(IsAvailableResponsePayload payload) {
        OnAvailabilityChecked event = new OnAvailabilityChecked(payload.type, payload.value, payload.isAvailable);

        if (payload.isError()) {
            event.error = payload.error;
        }

        emitChange(event);
    }

    /**
     * Can be used to check if Account is signed into Plutonem
     */
    public boolean hasAccessToken() {
        return mAccessToken.exists();
    }
}
