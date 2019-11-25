package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.AuthenticationAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.ActionBuilder;
import com.plutonem.android.fluxc.store.AccountStore.AuthenticateErrorPayload;

public final class AuthenticationActionBuilder extends ActionBuilder {
    public AuthenticationActionBuilder() {
    }

    public static Action<AuthenticateErrorPayload> newAuthenticateErrorAction(AuthenticateErrorPayload payload) {
        return new Action(AuthenticationAction.AUTHENTICATE_ERROR, payload);
    }
}
