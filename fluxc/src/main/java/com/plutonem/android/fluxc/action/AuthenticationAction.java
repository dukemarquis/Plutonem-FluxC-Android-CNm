package com.plutonem.android.fluxc.action;

import com.plutonem.android.fluxc.annotations.Action;
import com.plutonem.android.fluxc.annotations.ActionEnum;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.store.AccountStore.AuthenticatePayload;
import com.plutonem.android.fluxc.store.AccountStore.AuthenticateErrorPayload;

@ActionEnum
public enum AuthenticationAction implements IAction {
    // Remote actions
    @Action(payloadType = AuthenticatePayload.class)
    AUTHENTICATE,

    // Remote responses
    @Action(payloadType = AuthenticateErrorPayload.class)
    AUTHENTICATE_ERROR
}
