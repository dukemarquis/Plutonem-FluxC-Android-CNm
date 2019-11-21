package com.plutonem.android.fluxc.store;

import androidx.annotation.NonNull;

import com.android.volley.VolleyError;
import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.Payload;
import com.plutonem.android.fluxc.action.AccountAction;
import com.plutonem.android.fluxc.action.AuthenticationAction;
import com.plutonem.android.fluxc.action.RegistrationAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.network.BaseRequest.BaseNetworkError;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient.IsAvailable;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient.IsAvailableResponsePayload;
import com.plutonem.android.fluxc.network.rest.plutonem.auth.AccessToken;
import com.plutonem.android.fluxc.network.rest.plutonem.reg.Registor;
import com.plutonem.android.fluxc.network.rest.plutonem.reg.Registor.Token;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.util.AppLog;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In-memory based and persisted in SQLite.
 */
@Singleton
public class AccountStore extends Store {
    // Payloads
    public static class RegisterPayload extends Payload<BaseNetworkError> {
        public String phone;
        public String password;
        public String twoStepCode;
        public boolean shouldSendTwoStepSms;
        public Action nextAction;
        public RegisterPayload(@NonNull String phone, @NonNull String password) {
            this.phone = phone;
            this.password = password;
        }
    }

    // OnChanged Events
    public static class OnRegistrationChanged extends OnChanged<RegistrationError> {
        public String userName;
        public boolean createdAccount;
    }

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

    public static class RegistrationError implements OnChangedError {
        public RegistrationErrorType type;
        public String message;
        public RegistrationError(RegistrationErrorType type, @NonNull String message) {
            this.type = type;
            this.message = message;
        }
    }

    // Enums
    public enum RegistrationErrorType {
        // From response's "error" field
        INVALID_REQUEST,

        // From response's "message" field - sadly... (be careful with i18n)
        FAILURE_REGISTERING,

        // Generic error
        GENERIC_ERROR;

        public static RegistrationErrorType fromString(String string) {
            if (string != null) {
                for (RegistrationErrorType v : RegistrationErrorType.values()) {
                    if (string.equalsIgnoreCase(v.name())) {
                        return v;
                    }
                }
            }
            return GENERIC_ERROR;
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
    private Registor mRegistor;
    private AccessToken mAccessToken;

    @Inject
    public AccountStore(Dispatcher dispatcher, AccountRestClient accountRestClient,
                        AccessToken accessToken, Registor registor) {
        super(dispatcher);
        mRegistor = registor;
        mAccountRestClient = accountRestClient;
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
        if (actionType instanceof RegistrationAction) {
            onRegistrationAction((RegistrationAction) actionType, action.getPayload());
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

    private void onRegistrationAction(RegistrationAction actionType, Object payload) {
        switch (actionType) {
            case REGISTER:
                register((RegisterPayload) payload);
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

    private void register(final RegisterPayload payload) {
        mRegistor.register(payload.phone, payload.password, payload.twoStepCode,
                payload.shouldSendTwoStepSms, new Registor.Listener() {
                    @Override
                    public void onResponse(Token token) {
                        mAccessToken.set(token.getAccessToken());
                        if (payload.nextAction != null) {
                            mDispatcher.dispatch(payload.nextAction);
                        }
                        emitChange(new OnRegistrationChanged());
                    }
                }, new Registor.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        AppLog.e(AppLog.T.API, "Authentication error");
                        OnRegistrationChanged event = new OnRegistrationChanged();
                        event.error = new RegistrationError(
                                Registor.volleyErrorToRegistrationError(volleyError),
                                Registor.volleyErrorToErrorMessage(volleyError));
                        emitChange(event);
                    }
                });
    }
}
