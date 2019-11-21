package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.RegistrationAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.ActionBuilder;
import com.plutonem.android.fluxc.store.AccountStore.RegisterPayload;

public final class RegistrationActionBuilder extends ActionBuilder {
    public RegistrationActionBuilder() {
    }

    public static Action<RegisterPayload> newRegisterAction(RegisterPayload payload) {
        return new Action(RegistrationAction.REGISTER, payload);
    }
}
