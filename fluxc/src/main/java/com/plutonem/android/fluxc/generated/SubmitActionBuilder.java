package com.plutonem.android.fluxc.generated;

import com.plutonem.android.fluxc.action.SubmitAction;
import com.plutonem.android.fluxc.annotations.action.Action;
import com.plutonem.android.fluxc.annotations.action.ActionBuilder;
import com.plutonem.android.fluxc.store.OrderStore.RemoteOrderPayload;

public class SubmitActionBuilder extends ActionBuilder {
    public SubmitActionBuilder() {
    }

    public static Action<RemoteOrderPayload> newPushedOrderAction(RemoteOrderPayload payload) {
        return new Action(SubmitAction.PUSHED_ORDER, payload);
    }
}
