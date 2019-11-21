package com.plutonem.android.fluxc.action;

import com.plutonem.android.fluxc.annotations.Action;
import com.plutonem.android.fluxc.annotations.ActionEnum;
import com.plutonem.android.fluxc.annotations.action.IAction;
import com.plutonem.android.fluxc.store.AccountStore.RegisterPayload;

@ActionEnum
public enum RegistrationAction implements IAction {
    // Remote actions
    @Action(payloadType = RegisterPayload.class)
    REGISTER
}
