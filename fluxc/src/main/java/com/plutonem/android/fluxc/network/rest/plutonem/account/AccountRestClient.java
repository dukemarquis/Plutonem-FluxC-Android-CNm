package com.plutonem.android.fluxc.network.rest.plutonem.account;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.plutonem.android.fluxc.Dispatcher;
import com.plutonem.android.fluxc.Payload;
import com.plutonem.android.fluxc.action.AccountAction;
import com.plutonem.android.fluxc.generated.AccountActionBuilder;
import com.plutonem.android.fluxc.generated.endpoint.PLUTONEMREST;
import com.plutonem.android.fluxc.model.AccountModel;
import com.plutonem.android.fluxc.network.UserAgent;
import com.plutonem.android.fluxc.network.rest.plutonem.BasePlutonemRestClient;
import com.plutonem.android.fluxc.network.rest.plutonem.PlutonemGsonRequest;
import com.plutonem.android.fluxc.network.rest.plutonem.PlutonemGsonRequest.PlutonemErrorListener;
import com.plutonem.android.fluxc.network.rest.plutonem.PlutonemGsonRequest.PlutonemGsonNetworkError;
import com.plutonem.android.fluxc.network.rest.plutonem.auth.AccessToken;
import com.plutonem.android.fluxc.store.AccountStore.IsAvailableError;

import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class AccountRestClient extends BasePlutonemRestClient {
    public static class AccountRestPayload extends Payload<PlutonemGsonNetworkError> {
        public AccountRestPayload(AccountModel account, PlutonemGsonNetworkError error) {
            this.account = account;
            this.error = error;
        }
        public AccountModel account;
    }

    public static class IsAvailableResponsePayload extends Payload<IsAvailableError> {
        public IsAvailable type;
        public String value;
        public boolean isAvailable;
    }

    public enum IsAvailable {
        PHONE
    }

    public AccountRestClient(Context appContext, Dispatcher dispatcher, RequestQueue requestQueue,
                             AccessToken accessToken, UserAgent userAgent) {
        super(appContext, dispatcher, requestQueue, accessToken, userAgent);
    }

    /**
     * Performs an HTTP GET call to the v1.1 /me/ endpoint. Upon receiving a
     * response (success or error) a {@link AccountAction#FETCHED_ACCOUNT} action is dispatched
     * with a payload of type {@link AccountRestPayload}. {@link AccountRestPayload#isError()} can
     * be used to determine the result of the request.
     */
    public void fetchAccount() {
        String url = PLUTONEMREST.me.getUrlV1_1();
        add(PlutonemGsonRequest.buildGetRequest(url, null, AccountResponse.class,
                new Response.Listener<AccountResponse>() {
                    @Override
                    public void onResponse(AccountResponse response) {
                        AccountModel account = responseToAccountModel(response);
                        AccountRestPayload payload = new AccountRestPayload(account, null);
                        mDispatcher.dispatch(AccountActionBuilder.newFetchedAccountAction(payload));
                    }
                },
                new PlutonemErrorListener() {
                    @Override
                    public void onErrorResponse(@NonNull PlutonemGsonNetworkError error) {
                        AccountRestPayload payload = new AccountRestPayload(null, error);
                        mDispatcher.dispatch(AccountActionBuilder.newFetchedAccountAction(payload));
                    }
                }
        ));
    }

    /**
     * Performs an HTTP GET call to the v1.1 /me/settings/ endpoint. Upon receiving
     * a response (success or error) a {@link AccountAction#FETCHED_SETTINGS} action is dispatched
     * with a payload of type {@link AccountRestPayload}. {@link AccountRestPayload#isError()} can
     * be used to determine the result of the request.
     */
    public void fetchAccountSettings() {
        String url = PLUTONEMREST.me.settings.getUrlV1_1();
        add(PlutonemGsonRequest.buildGetRequest(url, null, AccountSettingsResponse.class,
                new Response.Listener<AccountSettingsResponse>() {
                    @Override
                    public void onResponse(AccountSettingsResponse response) {
                        AccountModel settings = responseToAccountSettingsModel(response);
                        AccountRestPayload payload = new AccountRestPayload(settings, null);
                        mDispatcher.dispatch(AccountActionBuilder.newFetchedSettingsAction(payload));
                    }
                },
                new PlutonemErrorListener() {
                    @Override
                    public void onErrorResponse(@NonNull PlutonemGsonNetworkError error) {
                        AccountRestPayload payload = new AccountRestPayload(null, error);
                        mDispatcher.dispatch(AccountActionBuilder.newFetchedSettingsAction(payload));
                    }
                }
        ));
    }

    public void isAvailable(@NonNull final String value, final IsAvailable type) {
        String url = "";
        switch (type) {
            case PHONE:
                url = PLUTONEMREST.is_available.phone.getUrlV0();
                break;
        }

        Map<String, String> params = new HashMap<>();
        params.put("q", value);

        PlutonemGsonRequest request = PlutonemGsonRequest.buildGetRequest(url, params, IsAvailableResponse.class,
                new Response.Listener<IsAvailableResponse>() {
                    @Override
                    public void onResponse(IsAvailableResponse response) {
                        IsAvailableResponsePayload payload = new IsAvailableResponsePayload();
                        payload.value = value;
                        payload.type = type;

                        if (response == null) {
                            // The 'is-available' endpoints return either true or a JSON object representing an error
                            // The JsonObjectOrFalseDeserializer will deserialize true to null, so a null response
                            // actually means that there were no errors and the queried item (e.g., email) is available
                            payload.isAvailable = true;
                        } else {
                            if (response.error.equals("taken")) {
                                // We consider "taken" not to be an error, and we report that the item is unavailable
                                payload.isAvailable = false;
                            } else {
                                // Genuine error (probably a malformed item)
                                payload.error = new IsAvailableError(response.error, response.message);
                            }
                        }
                        mDispatcher.dispatch(AccountActionBuilder.newCheckedIsAvailableAction(payload));
                    }
                },
                new PlutonemErrorListener() {
                    @Override
                    public void onErrorResponse(@NonNull PlutonemGsonNetworkError error) {
                        // We don't expect anything but server errors here - the API itself returns errors with a
                        // 200 status code, which will appear under Listener.onResponse instead
                        IsAvailableResponsePayload payload = new IsAvailableResponsePayload();
                        payload.value = value;
                        payload.type = type;

                        payload.error = new IsAvailableError(error.apiError, error.message);
                        mDispatcher.dispatch(AccountActionBuilder.newCheckedIsAvailableAction(payload));
                    }
                }
        );

        add(request);
    }

    private AccountModel responseToAccountModel(AccountResponse from) {
        AccountModel account = new AccountModel();
        account.setUserId(from.ID);
        account.setDisplayName(StringEscapeUtils.unescapeHtml4(from.display_name));
        account.setUserName(from.username);
        account.setPhone(from.phone);
        account.setDate(from.date);
        account.setHasUnseenNotes(from.has_unseen_notes);
        return account;
    }

    private AccountModel responseToAccountSettingsModel(AccountSettingsResponse from) {
        AccountModel account = new AccountModel();
        account.setPrimaryBuyerId(from.primary_buyer_ID);
        return account;
    }
}
