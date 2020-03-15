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
import com.plutonem.android.fluxc.model.AccountModel;
import com.plutonem.android.fluxc.network.BaseRequest.BaseNetworkError;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient.AccountRestPayload;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient.IsAvailable;
import com.plutonem.android.fluxc.network.rest.plutonem.account.AccountRestClient.IsAvailableResponsePayload;
import com.plutonem.android.fluxc.network.rest.plutonem.auth.AccessToken;
import com.plutonem.android.fluxc.network.rest.plutonem.auth.Authenticator;
import com.plutonem.android.fluxc.network.rest.plutonem.reg.Registor;
import com.plutonem.android.fluxc.network.rest.plutonem.reg.Registor.Token;
import com.plutonem.android.fluxc.persistence.AccountSqlUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.AppLog.T;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In-memory based and persisted in SQLite.
 */
@Singleton
public class AccountStore extends Store {
    // Payloads
    public static class AuthenticatePayload extends Payload<BaseNetworkError> {
        public String username;
        public String password;
        public String twoStepCode;
        public boolean shouldSendTwoStepSms;
        public Action nextAction;
        public AuthenticatePayload(@NonNull String username, @NonNull String password) {
            this.username = username;
            this.password = password;
        }
    }

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

    public static class AuthenticateErrorPayload extends Payload<AuthenticationError> {
        public AuthenticateErrorPayload(@NonNull AuthenticationError error) {
            this.error = error;
        }

        public AuthenticateErrorPayload(@NonNull AuthenticationErrorType errorType) {
            this.error = new AuthenticationError(errorType, "");
        }
    }

    // OnChanged Events
    public static class OnAccountChanged extends OnChanged<AccountError> {
        public boolean accountInfosChanged;
        public AccountAction causeOfChange;
    }

    public static class OnRegistrationChanged extends OnChanged<RegistrationError> {
        public String userName;
        public boolean createdAccount;
    }

    public static class OnAuthenticationChanged extends OnChanged<AuthenticationError> {
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

    public static class AuthenticationError implements OnChangedError {
        public AuthenticationErrorType type;
        public String message;

        public AuthenticationError(AuthenticationErrorType type, @NonNull String message) {
            this.type = type;
            this.message = message;
        }
    }

    // Enums
    public enum AuthenticationErrorType {
        // From response's "error" field
        AUTHORIZATION_REQUIRED,
        INVALID_REQUEST,
        INVALID_TOKEN,

        // From response's "message" field - sadly... (be careful with i18n)
        INCORRECT_USERNAME_OR_PASSWORD,

        // Generic error
        GENERIC_ERROR;

        public static AuthenticationErrorType fromString(String string) {
            if (string != null) {
                for (AuthenticationErrorType v : AuthenticationErrorType.values()) {
                    if (string.equalsIgnoreCase(v.name())) {
                        return v;
                    }
                }
            }
            return GENERIC_ERROR;
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

    public static class AccountError implements OnChangedError {
        public AccountErrorType type;
        public String message;
        public AccountError(AccountErrorType type, @NonNull String message) {
            this.type = type;
            this.message = message;
        }
    }

    public enum AccountErrorType {
        ACCOUNT_FETCH_ERROR,
        SETTINGS_FETCH_GENERIC_ERROR,
        SETTINGS_FETCH_REAUTHORIZATION_REQUIRED_ERROR
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
    private Authenticator mAuthenticator;
    private Registor mRegistor;
    private AccountModel mAccount;
    private AccessToken mAccessToken;

    @Inject
    public AccountStore(Dispatcher dispatcher, AccountRestClient accountRestClient,
                        Authenticator authenticator, Registor registor, AccessToken accessToken) {
        super(dispatcher);
        mAuthenticator = authenticator;
        mRegistor = registor;
        mAccountRestClient = accountRestClient;
        mAccount = loadAccount();
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
        if (actionType instanceof RegistrationAction) {
            onRegistrationAction((RegistrationAction) actionType, action.getPayload());
        }
        if (actionType instanceof AuthenticationAction) {
            onAuthenticationAction((AuthenticationAction) actionType, action.getPayload());
        }
    }

    private void onAccountAction(AccountAction actionType, Object payload) {
        switch (actionType) {
            case FETCH_ACCOUNT:
                mAccountRestClient.fetchAccount();
                break;
            case FETCH_SETTINGS:
                mAccountRestClient.fetchAccountSettings();
                break;
            case FETCHED_SETTINGS:
                handleFetchSettingsCompleted((AccountRestPayload) payload);
                break;
            case FETCHED_ACCOUNT:
                handleFetchAccountCompleted((AccountRestPayload) payload);
                break;
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

    private void onAuthenticationAction(AuthenticationAction actionType, Object payload) {
        switch (actionType) {
            case AUTHENTICATE:
                authenticate((AuthenticatePayload) payload);
                break;
            case AUTHENTICATE_ERROR:
                handleAuthenticateError((AuthenticateErrorPayload) payload);
                break;
        }
    }

    private void handleAuthenticateError(AuthenticateErrorPayload payload) {
        OnAuthenticationChanged event = new OnAuthenticationChanged();
        event.error = payload.error;
        emitChange(event);
    }

    private void handleFetchAccountCompleted(AccountRestPayload payload) {
        if (!hasAccessToken()) {
            emitAccountChangeError(AccountErrorType.ACCOUNT_FETCH_ERROR);
            return;
        }
        if (!checkError(payload, "Error fetching Account via REST API (/me)")) {
            mAccount.copyAccountAttributes(payload.account);
            updateDefaultAccount(mAccount, AccountAction.FETCH_ACCOUNT);
        } else {
            emitAccountChangeError(AccountErrorType.ACCOUNT_FETCH_ERROR);
        }
    }

    private void handleFetchSettingsCompleted(AccountRestPayload payload) {
        if (!hasAccessToken()) {
            emitAccountChangeError(AccountErrorType.SETTINGS_FETCH_GENERIC_ERROR);
            return;
        }
        if (!checkError(payload, "Error fetching Account Settings via REST API (/me/settings)")) {
            mAccount.copyAccountSettingsAttributes(payload.account);
            updateDefaultAccount(mAccount, AccountAction.FETCH_SETTINGS);
        }  else {
            OnAccountChanged accountChanged = new OnAccountChanged();
            accountChanged.causeOfChange = AccountAction.FETCH_SETTINGS;

            AccountErrorType errorType;
            if (payload.error.apiError.equals("reauthorization_required")) {
                // This error will always occur for 2FA accounts when using a non-production Plutonem OAuth client.
                // Essentially, some APIs around account management are disabled in those cases for security reasons.
                // The error is a bit generic from the server-side - it essentially means the user isn't privileged to
                // do the action and needs to reauthorize. For bearer token-based login, there is no escalation of
                // privileges possible, so the request just fails at that point.
                errorType = AccountErrorType.SETTINGS_FETCH_REAUTHORIZATION_REQUIRED_ERROR;
            } else {
                errorType = AccountErrorType.SETTINGS_FETCH_GENERIC_ERROR;
            }
            accountChanged.error = new AccountError(errorType, payload.error.message);

            emitChange(accountChanged);
        }
    }

    private void handleCheckedIsAvailable(IsAvailableResponsePayload payload) {
        OnAvailabilityChecked event = new OnAvailabilityChecked(payload.type, payload.value, payload.isAvailable);

        if (payload.isError()) {
            event.error = payload.error;
        }

        emitChange(event);
    }

    private void emitAccountChangeError(AccountErrorType errorType) {
        OnAccountChanged event = new OnAccountChanged();
        event.error = new AccountError(errorType, "");
        emitChange(event);
    }

    public AccountModel getAccount() {
        return mAccount;
    }

    /**
     * Can be used to check if Account is signed into Plutonem
     */
    public boolean hasAccessToken() {
        return mAccessToken.exists();
    }

    private void updateDefaultAccount(AccountModel accountModel, AccountAction cause) {
        // Update memory instance
        mAccount = accountModel;
        AccountSqlUtils.insertOrUpdateDefaultAccount(accountModel);
        OnAccountChanged accountChanged = new OnAccountChanged();
        accountChanged.accountInfosChanged = true;
        accountChanged.causeOfChange = cause;
        emitChange(accountChanged);
    }

    private AccountModel loadAccount() {
        AccountModel account = AccountSqlUtils.getDefaultAccount();
        return account == null ? new AccountModel() : account;
    }

    private void authenticate(final AuthenticatePayload payload) {
        mAuthenticator.authenticate(payload.username, payload.password, payload.twoStepCode,
                payload.shouldSendTwoStepSms, new Authenticator.Listener() {
                    @Override
                    public void onResponse(Token token) {
                        mAccessToken.set(token.getAccessToken());
                        if (payload.nextAction != null) {
                            mDispatcher.dispatch(payload.nextAction);
                        }
                        emitChange(new OnAuthenticationChanged());
                    }
                }, new Authenticator.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        AppLog.e(T.API, "Authentication error");
                        OnAuthenticationChanged event = new OnAuthenticationChanged();
                        event.error = new AuthenticationError(
                                Authenticator.volleyErrorToAuthenticationError(volleyError),
                                Authenticator.volleyErrorToErrorMessage(volleyError));
                        emitChange(event);
                    }
                });
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
                        AppLog.e(AppLog.T.API, "Registration error");
                        OnRegistrationChanged event = new OnRegistrationChanged();
                        event.error = new RegistrationError(
                                Registor.volleyErrorToRegistrationError(volleyError),
                                Registor.volleyErrorToErrorMessage(volleyError));
                        emitChange(event);
                    }
                });
    }

    private boolean checkError(AccountRestPayload payload, String log) {
        if (payload.isError()) {
            AppLog.w(T.API, log + "\nError: " + payload.error.volleyError);
            return true;
        }
        return false;
    }
}
